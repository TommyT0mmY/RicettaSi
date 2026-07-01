package it.unibo.psm.ricettasi.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import it.unibo.psm.ricettasi.data.local.entity.IngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {

    @Query("SELECT * FROM ingredients")
    suspend fun getAll(): List<IngredientEntity>

    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    fun observeAll(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE id = :id")
    suspend fun getById(id: String): IngredientEntity?

    /** Exact name match, case-insensitive. Global rows win over personal ones with the same name. */
    @Query("SELECT * FROM ingredients WHERE name = :name COLLATE NOCASE ORDER BY isGlobal DESC LIMIT 1")
    suspend fun findByName(name: String): IngredientEntity?

    /**
     * Prefix search over ingredient names. Callers pass the raw text the user typed: this turns it
     * into an FTS4 prefix query and ranks the hits so an exact name comes first, then the names
     * that start with the text, then the shortest ones.
     */
    suspend fun search(query: String, limit: Int): List<IngredientEntity> {
        // Keep only letters and digits so the input can't break the MATCH syntax, then append * to
        // each token for prefix search ("pomo" -> "pomo*", so typing "pomo" already matches
        // "pomodoro").
        val tokens = query.lowercase()
            .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
            .trim()
            .split(" ")
            .filter { it.isNotBlank() }
        if (tokens.isEmpty()) return emptyList()
        val match = tokens.joinToString(" ") { "$it*" }
        return searchFts(match, query.trim(), limit)
    }

    /** FTS-backed query for [search]. `match` is the FTS expression, `name` the raw typed text. */
    @Query("""
        SELECT ingredients.* FROM ingredients
        JOIN ingredient_fts ON ingredients.rowid = ingredient_fts.rowid
        WHERE ingredient_fts MATCH :match
        ORDER BY
            (ingredients.name = :name COLLATE NOCASE) DESC,
            (ingredients.name LIKE :name || '%') DESC,
            length(ingredients.name) ASC,
            ingredients.name ASC
        LIMIT :limit
    """)
    suspend fun searchFts(match: String, name: String, limit: Int): List<IngredientEntity>

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
