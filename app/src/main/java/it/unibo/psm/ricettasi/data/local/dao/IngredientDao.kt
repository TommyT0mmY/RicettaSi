package it.unibo.psm.ricettasi.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import it.unibo.psm.ricettasi.data.local.entity.IngredientEntity

@Dao
interface IngredientDao {

    @Query("SELECT * FROM ingredients")
    suspend fun getAll(): List<IngredientEntity>

    @Query("SELECT * FROM ingredients WHERE id = :id")
    suspend fun getById(id: String): IngredientEntity?

    /** Full-text search with FTS4. */
    @Query("""
        SELECT ingredients.* FROM ingredients
        JOIN ingredient_fts ON ingredients.rowid = ingredient_fts.rowid
        WHERE ingredient_fts MATCH :query
        LIMIT :limit
    """)
    suspend fun search(query: String, limit: Int): List<IngredientEntity>

    @Upsert
    suspend fun upsertAll(items: List<IngredientEntity>)

    /** For internal use. Deletes all ingredients of the given scope (global or personal). */
    @Query("DELETE FROM ingredients WHERE isGlobal = :global")
    suspend fun deleteByScope(global: Boolean)

    /** Replaces the entire scope (global or personal). Used by the refresh. */
    @Transaction
    suspend fun replaceScope(global: Boolean, items: List<IngredientEntity>) {
        require(items.all { it.isGlobal == global }) {
            "All ingredients must have isGlobal = $global"
        }
        deleteByScope(global)
        upsertAll(items)
    }
}
