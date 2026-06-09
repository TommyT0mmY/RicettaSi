package it.unibo.psm.ricettasi.domain.repository

import it.unibo.psm.ricettasi.domain.model.Recipe
import it.unibo.psm.ricettasi.domain.model.RecipeFilters
import it.unibo.psm.ricettasi.domain.model.RecipeWithAvailability
import kotlinx.coroutines.flow.Flow

/**
 * Recipes: local cache of favourited/viewed recipes, remote search and suggestions (recipes are
 * never fully downloaded), and the "Cucinato" gamification action.
 */
interface RecipeRepository {

    /** Recipe from the local cache, with relations. Returns null if it was never favourited or loaded. */
    suspend fun getRecipe(id: String): Recipe?

    /** Reactive: cross-device sync or Dettaglio unfavorite updates Preferiti list. */
    fun observeFavorites(): Flow<List<Recipe>>

    /** Reactive: sync pull replaces favorites, heart icon updates automatically. */
    fun observeIsFavorite(recipeId: String): Flow<Boolean>

    /** Adds or removes the recipe from favourites. */
    suspend fun setFavorite(recipeId: String, favorite: Boolean)

    /**
     * Binary availability of the recipe against the pantry (match on root ids).
     * Returns (availableCount, requiredCount).
     */
    suspend fun computeAvailability(recipeId: String): Pair<Int, Int>

    /** Searches remote recipes with optional filters. Pantry matching is done server-side. */
    suspend fun searchRecipes(query: String, filters: RecipeFilters = RecipeFilters()): List<RecipeWithAvailability>

    /**
     * Both Home lists ("Svuota il frigo" + "Per te") from two remote reads.
     * Returns (emptyFridge, suggestions).
     */
    suspend fun homeSuggestions(
        emptyFridgeLimit: Int = 10,
        suggestionsLimit: Int = 10,
    ): Pair<List<RecipeWithAvailability>, List<RecipeWithAvailability>>

    /** Loads a full recipe from the remote and caches it locally. Falls back to the local cache if the remote call fails. */
    suspend fun loadAndCacheRecipe(id: String): Recipe?

    /** Records "Cucinato": awards XP and re-evaluates badges server-side. */
    suspend fun recordCooked(recipeId: String)
}
