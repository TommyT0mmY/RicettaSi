package it.unibo.psm.ricettasi.data.sync

import android.util.Log
import io.github.jan.supabase.exceptions.RestException
import it.unibo.psm.ricettasi.data.local.dao.BadgeDao
import it.unibo.psm.ricettasi.data.local.dao.CategoryDao
import it.unibo.psm.ricettasi.data.local.dao.CookedRecipeDao
import it.unibo.psm.ricettasi.data.local.dao.FavoriteDao
import it.unibo.psm.ricettasi.data.local.dao.IngredientDao
import it.unibo.psm.ricettasi.data.local.dao.PantryDao
import it.unibo.psm.ricettasi.data.local.dao.ProfileDao
import it.unibo.psm.ricettasi.data.local.dao.RecipeDao
import it.unibo.psm.ricettasi.data.local.dao.SyncQueueDao
import it.unibo.psm.ricettasi.data.local.entity.SyncQueueEntity
import it.unibo.psm.ricettasi.data.mapper.toCategoryEntities
import it.unibo.psm.ricettasi.data.mapper.toDomain
import it.unibo.psm.ricettasi.data.mapper.toEntity
import it.unibo.psm.ricettasi.data.mapper.toIngredientEntities
import it.unibo.psm.ricettasi.data.mapper.toMealTypeEntities
import it.unibo.psm.ricettasi.data.remote.datasource.CookedRecipeRemoteDataSource
import it.unibo.psm.ricettasi.data.remote.datasource.FavoriteRemoteDataSource
import it.unibo.psm.ricettasi.data.remote.datasource.IngredientRemoteDataSource
import it.unibo.psm.ricettasi.data.remote.datasource.PantryRemoteDataSource
import it.unibo.psm.ricettasi.data.remote.datasource.RecipeRemoteDataSource
import it.unibo.psm.ricettasi.data.remote.datasource.SyncVersionsRemoteDataSource
import it.unibo.psm.ricettasi.data.remote.datasource.UserRemoteDataSource
import it.unibo.psm.ricettasi.data.remote.dto.CookedRecipeDto
import it.unibo.psm.ricettasi.data.remote.dto.FavoriteDto
import it.unibo.psm.ricettasi.data.remote.dto.IngredientDto
import it.unibo.psm.ricettasi.data.remote.dto.PantryItemDto
import it.unibo.psm.ricettasi.domain.repository.SessionRepository

/**
 * Orchestrator for pull-based synchronization.
 *
 * Each run does, in order: (a) drains the `SyncQueue` by pushing offline writes to Supabase,
 * then (b) performs a version-gated refresh by comparing counters returned by
 * `get_sync_versions()` against the stored ones and re-downloading only changed scopes.
 */
class SyncManager(
    private val syncVersionRemote: SyncVersionsRemoteDataSource,
    private val ingredientRemote: IngredientRemoteDataSource,
    private val pantryRemote: PantryRemoteDataSource,
    private val favoriteRemote: FavoriteRemoteDataSource,
    private val cookedRecipeRemote: CookedRecipeRemoteDataSource,
    private val userRemote: UserRemoteDataSource,
    private val recipeRemote: RecipeRemoteDataSource,
    private val versionStore: SyncVersionStore,
    private val session: SessionRepository,
    private val syncLock: SyncLock,
    private val ingredientDao: IngredientDao,
    private val pantryDao: PantryDao,
    private val favoriteDao: FavoriteDao,
    private val cookedRecipeDao: CookedRecipeDao,
    private val badgeDao: BadgeDao,
    private val profileDao: ProfileDao,
    private val recipeDao: RecipeDao,
    private val categoryDao: CategoryDao,
    private val syncQueueDao: SyncQueueDao,
) {

    private val mutex get() = syncLock.mutex

    // Returns true when there is nothing left to retry: the sync completed, another instance is
    // already running, or no user is logged in (in which case a sync is triggered again right
    // after login). Returns false only on transient failures (no network, 5xx) so WorkManager
    // can retry later.
    suspend fun sync(): Boolean {
        if (!mutex.tryLock()) return true
        return try {
            // No session yet (logged out, or still restoring at cold start). Retrying would not
            // help: the login transition triggers its own sync once a user is available.
            val userId = session.currentUserId() ?: return true
            // Pull replaces local tables, so drain the queue first to avoid losing offline writes.
            val drained = drainQueue()
            if (drained) versionGatedPull(userId)
            drained
        } catch (e: Exception) {
            Log.w(TAG, "Sync failed, will be retried", e)
            false
        } finally {
            mutex.unlock()
        }
    }

    /**
     * Pushes the queued writes to the server without a full refresh. Called right after a local
     * write so that, when online, the change reaches Supabase straight away; when offline it just
     * fails fast and the rows stay queued for the next sync. Shares the lock with [sync] so a write
     * landing during a full sync simply lets that sync drain the queue.
     */
    suspend fun pushPending() {
        if (!mutex.tryLock()) return
        try {
            drainQueue()
        } finally {
            mutex.unlock()
        }
    }

    // ---------- push: offline queue drain (FIFO) ----------

    /** @return `true` if the queue was fully drained, `false` if interrupted (likely no network). */
    private suspend fun drainQueue(): Boolean {
        val pending = syncQueueDao.getPending()
        for (op in pending) {
            try {
                dispatch(op)
                syncQueueDao.deleteById(op.id)
            } catch (e: RestException) {
                if (e.statusCode in 400..499) {
                    // Deterministic server rejection (4xx): the operation is "poisoned" and will not
                    // resolve on its own. Delete it so it doesn't block the queue.
                    Log.e(TAG, "Push (${op.payload::class.simpleName}/${op.action}) rejected by server, operation discarded", e)
                    syncQueueDao.deleteById(op.id)
                } else {
                    // 5xx or other: likely transient, abort and retry later.
                    Log.w(TAG, "Push (${op.payload::class.simpleName}/${op.action}) server error ${e.statusCode}, aborting drain", e)
                    return false
                }
            } catch (e: Exception) {
                // Likely no network: abort; remaining items stay in the queue for the
                // next attempt (preserving FIFO order).
                Log.w(TAG, "Queued push failed (${op.payload::class.simpleName}/${op.action}), aborting drain", e)
                return false
            }
        }
        return true
    }

    private suspend fun dispatch(op: SyncQueueEntity) {
        when (val dto = op.payload) {
            is PantryItemDto -> when (op.action) {
                SyncAction.UPSERT -> pantryRemote.upsertPantryItem(dto)
                SyncAction.DELETE -> pantryRemote.deletePantryItem(dto.id)
            }
            is FavoriteDto -> when (op.action) {
                SyncAction.UPSERT -> favoriteRemote.upsertFavorite(dto)
                SyncAction.DELETE -> favoriteRemote.deleteFavorite(dto.userId, dto.recipeId)
            }
            is CookedRecipeDto -> when (op.action) {
                SyncAction.UPSERT -> cookedRecipeRemote.upsertCookedRecipe(dto)
                SyncAction.DELETE -> cookedRecipeRemote.deleteCookedRecipe(dto.id)
            }
            is IngredientDto -> when (op.action) {
                SyncAction.UPSERT -> ingredientRemote.upsertIngredient(dto)
                SyncAction.DELETE -> ingredientRemote.deleteIngredient(dto.id)
            }
        }
    }

    // ---------- pull: refresh version-gated ----------

    private suspend fun versionGatedPull(userId: String) {
        val (localGlobal, localPersonal, localData) = versionStore.current() //SyncVersion
        val (remoteGlobal, remotePersonal, remoteData) = syncVersionRemote.getSyncVersions() //SyncVersionDto
        if (remoteGlobal != localGlobal) {
            refreshGlobalIngredients()
            refreshCategories()
            versionStore.setGlobal(remoteGlobal)
        }
        val personal = remotePersonal ?: 0L
        if (personal != localPersonal) {
            refreshPersonalIngredients(userId)
            versionStore.setPersonal(personal)
        }
        val data = remoteData ?: 0L
        if (data != localData) {
            refreshUserData(userId)
            versionStore.setData(data)
        }
    }

    private suspend fun refreshGlobalIngredients() {
        val items = ingredientRemote.fetchGlobalIngredients().map { it.toEntity() }
        ingredientDao.replaceScope(global = true, items = items)
    }

    private suspend fun refreshCategories() {
        categoryDao.clear()
        categoryDao.upsertAll(userRemote.fetchCategories().map { it.toEntity() })
    }

    private suspend fun refreshPersonalIngredients(userId: String) {
        val items = ingredientRemote.fetchPersonalIngredients(userId).map { it.toEntity() }
        ingredientDao.replaceScope(global = false, items = items)
    }

    /** Re-downloads all user-scoped tables (small) and replaces the local copy. */
    private suspend fun refreshUserData(userId: String) {
        val pantry = pantryRemote.fetchPantry(userId).map { it.toDomain().toEntity() }
        pantryDao.clear()
        pantryDao.upsertAll(pantry)

        val favorites = favoriteRemote.fetchFavorites(userId).map { it.toEntity() }
        favoriteDao.clear()
        favoriteDao.upsertAll(favorites)
        // `observeFavorites` does an INNER JOIN on `recipes`: download missing recipe bodies
        // (e.g. favourites created on another device) so they don't disappear from the list.
        cacheMissingRecipes(favorites.map { it.recipeId })

        val cooked = cookedRecipeRemote.fetchCookedRecipes(userId).map { it.toEntity() }
        cookedRecipeDao.clear()
        cookedRecipeDao.upsertAll(cooked)

        val userBadges = userRemote.fetchUserBadges(userId).map { it.toDomain().toEntity() }
        badgeDao.clearUserBadges()
        badgeDao.upsertUserBadges(userBadges)

        // The badge list is global data but tiny: refresh it at the same time.
        badgeDao.upsertBadges(userRemote.fetchBadges().map { it.toDomain().toEntity() })

        userRemote.fetchProfile(userId)?.let { profileDao.upsert(it.toDomain().toEntity()) }
    }

    /** Downloads and caches the bodies of the given recipes not yet present locally. */
    private suspend fun cacheMissingRecipes(recipeIds: List<String>) {
        if (recipeIds.isEmpty()) return
        val cached = recipeDao.existingIds(recipeIds).toSet()
        val categoryNames = categoryDao.getAll().associate { it.id to it.name }
        for (id in recipeIds.toSet() - cached) {
            val recipe = recipeRemote.getById(id)?.toDomain(categoryNames) ?: continue
            recipeDao.upsertFull(
                recipe = recipe.toEntity(),
                ingredients = recipe.toIngredientEntities(),
                mealTypes = recipe.toMealTypeEntities(),
                categories = recipe.toCategoryEntities(),
            )
        }
    }

    companion object {
        private const val TAG = "SyncManager"
    }
}
