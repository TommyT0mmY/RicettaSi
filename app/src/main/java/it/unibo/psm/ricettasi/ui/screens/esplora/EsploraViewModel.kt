package it.unibo.psm.ricettasi.ui.screens.esplora

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.psm.ricettasi.domain.model.Difficulty
import it.unibo.psm.ricettasi.domain.model.Ingredient
import it.unibo.psm.ricettasi.domain.model.MealType
import it.unibo.psm.ricettasi.domain.model.RecipeFilters
import it.unibo.psm.ricettasi.domain.model.RecipeWithAvailability
import it.unibo.psm.ricettasi.domain.model.TimeWindow
import it.unibo.psm.ricettasi.domain.repository.IngredientRepository
import it.unibo.psm.ricettasi.domain.repository.PantryRepository
import it.unibo.psm.ricettasi.domain.repository.RecipeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Which of the four Esplora pages is currently shown. */
enum class EsploraMode { MAIN, FILTERS, INGREDIENT_SEARCH, CATEGORY_SEARCH }

data class EsploraUiState(
    val mode: EsploraMode = EsploraMode.MAIN,
    val query: String = "",
    // set when the filter menu is opened from the search bar, so the query field grabs focus
    val requestSearchFocus: Boolean = false,
    // -- ingredient search sub-page --
    val ingredientQuery: String = "",
    val ingredientSuggestions: List<Ingredient> = emptyList(),
    val pantryIngredients: List<Ingredient> = emptyList(),
    val selectedIngredients: List<Ingredient> = emptyList(),
    // -- category search sub-page --
    val categoryQuery: String = "",
    val allCategories: List<String> = emptyList(),
    val selectedCategories: Set<String> = emptySet(),
    // -- chip filters --
    val mealType: MealType? = null,
    val timeWindows: Set<TimeWindow> = emptySet(),
    val difficulties: Set<Difficulty> = emptySet(),
    // -- results --
    val results: List<RecipeWithAvailability> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val isSearching: Boolean = false,
) {
    /** Number of filters the user has applied, shown on the "Mostra risultati (N)" button. */
    val appliedFilterCount: Int
        get() = selectedIngredients.size + selectedCategories.size + timeWindows.size +
            difficulties.size + (if (mealType != null) 1 else 0)

    val resultCount: Int get() = results.size

    /** Categories filtered by the category-search query (for the "Sfoglia" page). */
    val visibleCategories: List<String>
        get() = if (categoryQuery.isBlank()) allCategories
        else allCategories.filter { it.contains(categoryQuery.trim(), ignoreCase = true) }
}

/**
 * ViewModel for the Esplora flow. Keeps the filter state and a live list of matching recipes:
 * the main page shows the results, a filter menu (opened from the search bar) lets the user
 * compose filters, and two sub-pages add ingredients (with pantry quick-add) and categories.
 */
class EsploraViewModel(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val pantryRepository: PantryRepository,
    private val filterBus: EsploraFilterBus,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EsploraUiState())
    val uiState: StateFlow<EsploraUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var ingredientSearchJob: Job? = null
    private var isFirstSearch = true

    init {
        viewModelScope.launch {
            val names = runCatching { recipeRepository.getCategoryNames() }.getOrDefault(emptyList())
            _uiState.update { it.copy(allCategories = names) }
        }
        viewModelScope.launch {
            recipeRepository.observeFavoriteIds().collect { ids ->
                _uiState.update { it.copy(favoriteIds = ids.toSet()) }
            }
        }
        viewModelScope.launch {
            combine(
                pantryRepository.observeActive(),
                ingredientRepository.observeAll(),
            ) { items, ingredients ->
                val byId = ingredients.associateBy { it.id }
                items.mapNotNull { byId[it.ingredientId] }.distinctBy { it.id }
            }.collect { pantry ->
                _uiState.update { it.copy(pantryIngredients = pantry) }
            }
        }
        viewModelScope.launch {
            filterBus.pending.collect { preset ->
                if (preset != null) {
                    applyPreset(preset)
                    filterBus.consume()
                }
            }
        }
        runSearch()
    }

    /** Applies a set of filters coming from a Home "Vedi tutto" and lands on the results list. */
    private fun applyPreset(preset: EsploraPreset) {
        _uiState.update {
            it.copy(
                mode = EsploraMode.MAIN,
                query = "",
                selectedIngredients = emptyList(),
                selectedCategories = emptySet(),
                mealType = preset.mealType,
                timeWindows = preset.timeWindow?.let { w -> setOf(w) } ?: emptySet(),
                difficulties = preset.difficulties,
            )
        }
        runSearch()
    }

    // --- navigation between the four pages ---

    fun openFilters() = _uiState.update { it.copy(mode = EsploraMode.FILTERS) }

    /** Open the filter menu from the search bar: focus the "Cerca ricette" field straight away. */
    fun openSearch() = _uiState.update { it.copy(mode = EsploraMode.FILTERS, requestSearchFocus = true) }

    fun consumeSearchFocus() = _uiState.update { it.copy(requestSearchFocus = false) }

    fun openIngredientSearch() = _uiState.update {
        it.copy(mode = EsploraMode.INGREDIENT_SEARCH, ingredientQuery = "", ingredientSuggestions = emptyList())
    }

    fun openCategorySearch() = _uiState.update {
        it.copy(mode = EsploraMode.CATEGORY_SEARCH, categoryQuery = "")
    }

    fun backToFilters() = _uiState.update { it.copy(mode = EsploraMode.FILTERS) }

    /** "Mostra risultati": the search already ran live, so just close the menu back to the main list. */
    fun applyAndShowResults() = _uiState.update { it.copy(mode = EsploraMode.MAIN) }

    // --- text query ---

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(query = query) }
        runSearch()
    }

    // --- ingredient search ---

    fun onIngredientQueryChanged(query: String) {
        _uiState.update { it.copy(ingredientQuery = query) }
        ingredientSearchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(ingredientSuggestions = emptyList()) }
            return
        }
        ingredientSearchJob = viewModelScope.launch {
            val results = runCatching { ingredientRepository.search(query, limit = 10) }.getOrDefault(emptyList())
            val added = _uiState.value.selectedIngredients.map { it.id }.toSet()
            _uiState.update { it.copy(ingredientSuggestions = results.filter { r -> r.id !in added }) }
        }
    }

    fun addIngredient(ingredient: Ingredient) {
        _uiState.update {
            if (it.selectedIngredients.any { i -> i.id == ingredient.id }) it
            else it.copy(
                selectedIngredients = it.selectedIngredients + ingredient,
                ingredientSuggestions = it.ingredientSuggestions.filter { s -> s.id != ingredient.id },
            )
        }
        runSearch()
    }

    /** Pill in the filter menu: re-clicking removes the ingredient from the filter. */
    fun removeIngredient(ingredient: Ingredient) {
        _uiState.update {
            it.copy(selectedIngredients = it.selectedIngredients.filter { i -> i.id != ingredient.id })
        }
        runSearch()
    }

    /** Pantry row in the ingredient sub-page: stays visible, tapping selects/deselects it. */
    fun toggleIngredient(ingredient: Ingredient) {
        if (_uiState.value.selectedIngredients.any { it.id == ingredient.id }) removeIngredient(ingredient)
        else addIngredient(ingredient)
    }

    // --- category search ---

    fun onCategoryQueryChanged(query: String) {
        _uiState.update { it.copy(categoryQuery = query) }
    }

    fun addCategory(category: String) {
        _uiState.update { it.copy(selectedCategories = it.selectedCategories + category) }
        runSearch()
    }

    /** Pill in the filter menu: re-clicking removes the category from the filter. */
    fun removeCategory(category: String) {
        _uiState.update { it.copy(selectedCategories = it.selectedCategories - category) }
        runSearch()
    }

    /** Row in the category sub-page: stays visible, tapping selects/deselects it. */
    fun toggleCategory(category: String) {
        if (category in _uiState.value.selectedCategories) removeCategory(category) else addCategory(category)
    }

    // --- chip filters ---

    fun toggleMealType(mealType: MealType) {
        _uiState.update { it.copy(mealType = if (it.mealType == mealType) null else mealType) }
        runSearch()
    }

    fun toggleTimeWindow(window: TimeWindow) {
        _uiState.update { it.copy(timeWindows = it.timeWindows.toggle(window)) }
        runSearch()
    }

    fun toggleDifficulty(difficulty: Difficulty) {
        _uiState.update { it.copy(difficulties = it.difficulties.toggle(difficulty)) }
        runSearch()
    }

    fun reset() {
        _uiState.update {
            it.copy(
                query = "",
                selectedIngredients = emptyList(),
                selectedCategories = emptySet(),
                mealType = null,
                timeWindows = emptySet(),
                difficulties = emptySet(),
            )
        }
        runSearch()
    }

    fun toggleFavorite(recipeId: String) {
        viewModelScope.launch {
            val isFavorite = recipeId in _uiState.value.favoriteIds
            try {
                recipeRepository.setFavorite(recipeId, !isFavorite)
            } catch (_: Exception) {
                // silently ignore
            }
        }
    }

    // --- search ---

    private fun runSearch() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (!isFirstSearch) delay(250) else isFirstSearch = false
            _uiState.update { it.copy(isSearching = true) }
            val state = _uiState.value
            val filters = RecipeFilters(
                mealType = state.mealType,
                difficulties = state.difficulties,
                timeWindows = state.timeWindows,
                categories = state.selectedCategories,
                ingredientIds = state.selectedIngredients.map { it.id }.toSet(),
            )
            val results = try {
                recipeRepository.searchRecipes(state.query, filters)
            } catch (_: CancellationException) {
                throw CancellationException()
            } catch (_: Exception) {
                emptyList()
            }
            _uiState.update { it.copy(results = results, isSearching = false) }
        }
    }

    private fun <T> Set<T>.toggle(value: T): Set<T> =
        if (value in this) this - value else this + value
}
