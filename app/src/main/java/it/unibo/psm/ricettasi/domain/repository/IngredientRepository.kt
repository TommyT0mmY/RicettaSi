package it.unibo.psm.ricettasi.domain.repository

import it.unibo.psm.ricettasi.domain.model.Ingredient

/**
 * The ingredient list: lookup, fuzzy search and personal ingredients added by the user.
 */
interface IngredientRepository {

    /** Local FTS4 prefix search. Returns at most [limit] results ranked by FTS4 relevance. */
    suspend fun search(query: String, limit: Int = 8): List<Ingredient>

    /** Retrieves a single ingredient by id, or null if it does not exist. */
    suspend fun getById(id: String): Ingredient?

    /** Creates a new personal ingredient (`created_by_user = true`) with the provided name as-is. */
    suspend fun createPersonal(name: String): Ingredient
}
