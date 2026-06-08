package it.unibo.psm.ricettasi.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import it.unibo.psm.ricettasi.data.local.entity.IngredientCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {

    @Query("SELECT * FROM ingredient_cache")
    suspend fun getAll(): List<IngredientCacheEntity>

    @Query("SELECT * FROM ingredient_cache")
    fun observeAll(): Flow<List<IngredientCacheEntity>>

    @Query("SELECT * FROM ingredient_cache WHERE id = :id")
    suspend fun getById(id: String): IngredientCacheEntity?

    /**
     * Ricerca full-text con FTS4 (prefix match).
     * Il carattere `*` abilita il prefix matching: "pomo" -> "pomodoro", "pomodorini", etc.
     */
    @Query("""
        SELECT ingredient_cache.* FROM ingredient_cache
        JOIN ingredient_cache_fts ON ingredient_cache.rowid = ingredient_cache_fts.rowid
        WHERE ingredient_cache_fts MATCH :query
        LIMIT :limit
    """)
    suspend fun search(query: String, limit: Int): List<IngredientCacheEntity>

    @Upsert
    suspend fun upsertAll(items: List<IngredientCacheEntity>)

    @Query("DELETE FROM ingredient_cache WHERE isGlobal = :global")
    suspend fun deleteByScope(global: Boolean)

    /** Rimpiazza interamente lo scope (globale o personale). Usato dal refresh. */
    @Transaction
    suspend fun replaceScope(global: Boolean, items: List<IngredientCacheEntity>) {
        deleteByScope(global)
        upsertAll(items)
    }
}
