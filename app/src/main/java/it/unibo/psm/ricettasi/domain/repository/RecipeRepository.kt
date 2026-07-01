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

    /** Reactive ids of every favourited recipe, even ones not in the local cache. Drives the heart on list cards. */
    fun observeFavoriteIds(): Flow<List<String>>

    /** Reactive: sync pull replaces favorites, heart icon updates automatically. */
    fun observeIsFavorite(recipeId: String): Flow<Boolean>

    /** Adds or removes the recipe from favourites. */
    suspend fun setFavorite(recipeId: String, favorite: Boolean)

    /**
     * Binary availability of the recipe against the pantry (match on root ids).
     * Returns (availableCount, requiredCount).
     */
    suspend fun computeAvailability(recipeId: String): Pair<Int, Int>

    /** The ids of the recipe's ingredients that are currently in the pantry (matched on root ids). */
    suspend fun availableIngredientIds(recipeId: String): Set<String>

    /** Searches remote recipes with optional filters. Pantry matching is done server-side. */
    suspend fun searchRecipes(query: String, filters: RecipeFilters = RecipeFilters()): List<RecipeWithAvailability>

    /** All category names from the local cache, used to build the Esplora filter chips. */
    suspend fun getCategoryNames(): List<String>

    /**
     * Recipes for the "Svuota il frigo" page: only those that use at least one expiring pantry
     * ingredient, ranked by how urgent those ingredients are (order_by = expiring).
     */
    suspend fun emptyFridgeRecipes(limit: Int = 30): List<RecipeWithAvailability>

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

    /** Whether the current user has cooked this recipe at least once. */
    fun observeIsCooked(recipeId: String): Flow<Boolean>
}
