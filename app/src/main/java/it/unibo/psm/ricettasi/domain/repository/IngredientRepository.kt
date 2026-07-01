package it.unibo.psm.ricettasi.domain.repository

import it.unibo.psm.ricettasi.domain.model.Ingredient
import kotlinx.coroutines.flow.Flow

/**
 * The ingredient list: lookup, fuzzy search and personal ingredients added by the user.
 */
interface IngredientRepository {

    /** Reactive stream of all ingredients (both global and personal). */
    fun observeAll(): Flow<List<Ingredient>>

    /** Local FTS4 prefix search. At most [limit] results, exact name first then prefix then shortest. */
    suspend fun search(query: String, limit: Int = 8): List<Ingredient>

    /** Retrieves a single ingredient by id, or null if it does not exist. */
    suspend fun getById(id: String): Ingredient?

    /** Exact case-insensitive name lookup, preferring the global catalog. Null if not found. */
    suspend fun findByName(name: String): Ingredient?

    /** Creates a new personal ingredient (`created_by_user = true`) with the provided name as-is. */
    suspend fun createPersonal(name: String): Ingredient
}
