package it.unibo.psm.ricettasi.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.psm.ricettasi.domain.model.Difficulty
import it.unibo.psm.ricettasi.domain.model.MealType
import it.unibo.psm.ricettasi.domain.model.RecipeFilters
import it.unibo.psm.ricettasi.domain.model.RecipeSummary
import it.unibo.psm.ricettasi.domain.model.RecipeWithAvailability
import it.unibo.psm.ricettasi.domain.model.TimeWindow
import it.unibo.psm.ricettasi.domain.model.toSummary
import it.unibo.psm.ricettasi.domain.repository.IngredientRepository
import it.unibo.psm.ricettasi.domain.repository.PantryRepository
import it.unibo.psm.ricettasi.domain.repository.RecipeRepository
import it.unibo.psm.ricettasi.ui.screens.pantry.PantryItemDisplay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

enum class TimeSlot {
    MATTINA, PRANZO, POMERIGGIO, CENA, NOTTE;

    companion object {
        fun current(): TimeSlot {
            val h = LocalTime.now().hour
            val m = LocalTime.now().minute
            val t = h * 60 + m
            return when {
                t in 360..629 -> MATTINA
                t in 630..839 -> PRANZO
                t in 840..1049 -> POMERIGGIO
                t in 1050..1319 -> CENA
                else -> NOTTE
            }
        }
    }

    val heroLine1: String
        get() = when (this) {
            MATTINA -> "Buongiorno,"
            PRANZO -> "Cosa cuciniamo"
            POMERIGGIO -> "Voglia di"
            CENA -> "Cosa cuciniamo"
            NOTTE -> "Uno spuntino"
        }

    val heroLine2: String
        get() = when (this) {
            MATTINA -> "cosa cuciniamo?"
            PRANZO -> "a pranzo?"
            POMERIGGIO -> "uno spuntino?"
            CENA -> "stasera?"
            NOTTE -> "notturno?"
        }

    val sectionTitle: String
        get() = when (this) {
            MATTINA -> "Idee per la colazione"
            PRANZO -> "Pausa pranzo"
            POMERIGGIO -> "Merenda golosa"
            CENA -> "Idee per la cena"
            NOTTE -> "Spuntino notturno"
        }

    val mealType: MealType
        get() = when (this) {
            MATTINA -> MealType.COLAZIONE
            PRANZO -> MealType.PRANZO
            POMERIGGIO -> MealType.MERENDA
            CENA -> MealType.CENA
            NOTTE -> MealType.APERITIVO
        }
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val expiringItems: List<PantryItemDisplay> = emptyList(),
    val suggestions: List<RecipeWithAvailability> = emptyList(),
    val favorites: List<RecipeSummary> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val quickRecipes: List<RecipeWithAvailability> = emptyList(),
    val timeSlot: TimeSlot = TimeSlot.current(),
    val timeBasedRecipes: List<RecipeWithAvailability> = emptyList(),
    val challengeRecipes: List<RecipeWithAvailability> = emptyList(),
)

class HomeViewModel(
    private val pantryRepository: PantryRepository,
    private val ingredientRepository: IngredientRepository,
    private val recipeRepository: RecipeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeExpiring()
        observeFavorites()
        observeFavoriteIds()
        reloadOnPantryChange()
    }

    private fun reloadOnPantryChange() {
        viewModelScope.launch {
            pantryRepository.observeActive().collect {
                loadRemoteData()
            }
        }
    }

    private fun observeExpiring() {
        viewModelScope.launch {
            combine(
                pantryRepository.observeActive(),
                ingredientRepository.observeAll(),
            ) { items, ingredients ->
                val byId = ingredients.associateBy { it.id }
                val today = LocalDate.now()
                items
                    .filter { item ->
                        val expiry = item.expiryDate ?: return@filter false
                        ChronoUnit.DAYS.between(today, expiry) <= 3
                    }
                    .sortedBy { it.expiryDate }
                    .map { item ->
                        PantryItemDisplay(
                            pantryItem = item,
                            ingredientName = byId[item.ingredientId]?.name ?: "...",
                        )
                    }
            }.collect { items ->
                _uiState.update { it.copy(expiringItems = items) }
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            recipeRepository.observeFavorites().collect { recipes ->
                _uiState.update { it.copy(favorites = recipes.map { r -> r.toSummary() }) }
            }
        }
    }

    private fun observeFavoriteIds() {
        viewModelScope.launch {
            recipeRepository.observeFavoriteIds().collect { ids ->
                _uiState.update { it.copy(favoriteIds = ids.toSet()) }
            }
        }
    }

    private fun loadRemoteData() {
        viewModelScope.launch {
            val slot = TimeSlot.current()
            _uiState.update { it.copy(isLoading = true, timeSlot = slot) }

            runCatching {
                val (_, suggestions) = recipeRepository.homeSuggestions(suggestionsLimit = 10)
                _uiState.update { it.copy(suggestions = suggestions) }
            }

            runCatching {
                val quick = recipeRepository.searchRecipes(
                    query = "",
                    filters = RecipeFilters(timeWindows = setOf(TimeWindow.QUICK)),
                )
                _uiState.update { it.copy(quickRecipes = quick) }
            }

            runCatching {
                val timeBased = recipeRepository.searchRecipes(
                    query = "",
                    filters = RecipeFilters(mealType = slot.mealType),
                )
                _uiState.update { it.copy(timeBasedRecipes = timeBased) }
            }

            runCatching {
                val challenge = recipeRepository.searchRecipes(
                    query = "",
                    filters = RecipeFilters(
                        difficulties = setOf(Difficulty.MEDIO, Difficulty.DIFFICILE),
                    ),
                )
                _uiState.update { it.copy(challengeRecipes = challenge) }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun refresh() {
        loadRemoteData()
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
}
