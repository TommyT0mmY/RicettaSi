package it.unibo.psm.ricettasi.data.repository

import android.util.Log
import it.unibo.psm.ricettasi.data.local.dao.CategoryDao
import it.unibo.psm.ricettasi.data.local.dao.CookedRecipeDao
import it.unibo.psm.ricettasi.data.local.dao.FavoriteDao
import it.unibo.psm.ricettasi.data.local.dao.IngredientDao
import it.unibo.psm.ricettasi.data.local.dao.PantryDao
import it.unibo.psm.ricettasi.data.local.dao.RecipeDao
import it.unibo.psm.ricettasi.data.local.entity.CookedRecipeEntity
import it.unibo.psm.ricettasi.data.local.entity.FavoriteEntity
import it.unibo.psm.ricettasi.data.mapper.toCategoryEntities
import it.unibo.psm.ricettasi.data.mapper.toDomain
import it.unibo.psm.ricettasi.data.mapper.toEntity
import it.unibo.psm.ricettasi.data.mapper.toIngredientEntities
import it.unibo.psm.ricettasi.data.mapper.toMealTypeEntities
import it.unibo.psm.ricettasi.data.remote.datasource.RecipeRemoteDataSource
import it.unibo.psm.ricettasi.data.remote.dto.CookedRecipeDto
import it.unibo.psm.ricettasi.data.remote.dto.FavoriteDto
import it.unibo.psm.ricettasi.data.sync.SyncAction
import it.unibo.psm.ricettasi.data.sync.SyncWriter
import it.unibo.psm.ricettasi.domain.model.Recipe
import it.unibo.psm.ricettasi.domain.model.RecipeFilters
import it.unibo.psm.ricettasi.domain.model.RecipeWithAvailability
import it.unibo.psm.ricettasi.domain.repository.RecipeRepository
import it.unibo.psm.ricettasi.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID

class RecipeRepositoryImpl(
    private val recipeDao: RecipeDao,
    private val favoriteDao: FavoriteDao,
    private val pantryDao: PantryDao,
    private val ingredientDao: IngredientDao,
    private val cookedRecipeDao: CookedRecipeDao,
    private val categoryDao: CategoryDao,
    private val recipeRemote: RecipeRemoteDataSource,
    private val syncWriter: SyncWriter,
    private val session: SessionRepository,
) : RecipeRepository {

    override suspend fun getRecipe(id: String): Recipe? =
        recipeDao.getRecipeWithRelations(id)?.toDomain()

    override fun observeFavorites(): Flow<List<Recipe>> =
        recipeDao.observeFavorites().map { list -> list.map { it.toDomain() } }

    override fun observeFavoriteIds(): Flow<List<String>> =
        favoriteDao.observeFavoriteIds()

    override fun observeIsFavorite(recipeId: String): Flow<Boolean> =
        favoriteDao.observeIsFavorite(recipeId)

    override suspend fun setFavorite(recipeId: String, favorite: Boolean) {
        val userId = session.currentUserId() ?: return
        val savedDate = Instant.now()
        val dto = FavoriteDto(userId = userId, recipeId = recipeId, savedDate = savedDate.toString())
        val action = if (favorite) SyncAction.UPSERT else SyncAction.DELETE
        syncWriter.write(action, dto) {
            if (favorite) {
                favoriteDao.upsert(FavoriteEntity(recipeId = recipeId, savedDate = savedDate.toEpochMilli()))
            } else {
                favoriteDao.deleteById(recipeId)
            }
        }
    }

    override suspend fun computeAvailability(recipeId: String): Pair<Int, Int> {
        val byId = ingredientDao.getAll().associateBy { it.id }
        val pantryRoots = pantryDao.getActive().map { resolveRoot(it.ingredientId, byId) }.toSet()
        val requiredRoots = recipeDao.getIngredients(recipeId)
            .map { resolveRoot(it.ingredientId, byId) }
            .toSet()
        return requiredRoots.count { it in pantryRoots } to requiredRoots.size
    }

    // ---------- search & suggestions (via RPC search_recipes) ----------

    override suspend fun searchRecipes(query: String, filters: RecipeFilters): List<RecipeWithAvailability> {
        val filterRoots = if (filters.ingredientIds.isEmpty()) emptyList()
            else {
                val byId = ingredientDao.getAll().associateBy { it.id }
                filters.ingredientIds.map { resolveRoot(it, byId) }
            }
        return recipeRemote.searchRecipes(
            query = query.ifBlank { null },
            difficulties = filters.difficulties.map { it.value },
            timeWindows = filters.timeWindows.map { it.code },
            mealType = filters.mealType?.value,
            categories = filters.categories.toList(),
            ingredientRoots = filterRoots,
            orderBy = "match",
        ).map { it.toDomain() }
    }

    override suspend fun getCategoryNames(): List<String> =
        categoryDao.getAll().map { it.name }.sorted()

    override suspend fun emptyFridgeRecipes(limit: Int): List<RecipeWithAvailability> =
        recipeRemote.searchRecipes(
            minExpiring = 1,
            orderBy = "expiring",
            limit = limit,
        ).map { it.toDomain() }

    override suspend fun homeSuggestions(
        emptyFridgeLimit: Int,
        suggestionsLimit: Int,
    ): Pair<List<RecipeWithAvailability>, List<RecipeWithAvailability>> {
        return Pair(
            recipeRemote.searchRecipes(
                minExpiring = 1,
                orderBy = "expiring",
                limit = emptyFridgeLimit,
            ).map { it.toDomain() },
            recipeRemote.searchRecipes(
                minAvailable = 1,
                orderBy = "match",
                limit = suggestionsLimit,
            ).map { it.toDomain() },
        )
    }

    // ---------- detail & cache ----------

    override suspend fun loadAndCacheRecipe(id: String): Recipe? {
        val categoryNames = categoryDao.getAll().associate { it.id to it.name }
        val remoteRecipe = runCatching { recipeRemote.getById(id)?.toDomain(categoryNames) }
            .onFailure { Log.w(TAG, "Fetch recipe $id failed, falling back to local cache", it) }
            .getOrNull()
        val recipe = remoteRecipe ?: getRecipe(id) ?: return null
        if (remoteRecipe != null) cacheRecipe(remoteRecipe)
        return recipe
    }

    override suspend fun recordCooked(recipeId: String) {
        val userId = session.currentUserId() ?: return
        val cookedDate = Instant.now()
        val id = UUID.randomUUID().toString()
        val dto = CookedRecipeDto(id = id, userId = userId, recipeId = recipeId, cookedDate = cookedDate.toString())
        syncWriter.write(SyncAction.UPSERT, dto) {
            cookedRecipeDao.upsert(CookedRecipeEntity(id = id, recipeId = recipeId, cookedDate = cookedDate.toEpochMilli()))
        }
    }

    override fun observeIsCooked(recipeId: String): Flow<Boolean> =
        cookedRecipeDao.observeExistsByRecipeId(recipeId)

    private suspend fun cacheRecipe(recipe: Recipe) {
        recipeDao.upsertFull(
            recipe = recipe.toEntity(),
            ingredients = recipe.toIngredientEntities(),
            mealTypes = recipe.toMealTypeEntities(),
            categories = recipe.toCategoryEntities(),
        )
    }

    companion object {
        private const val TAG = "RecipeRepository"
    }
}
