package it.unibo.psm.ricettasi.ui.screens.svuotailfrigo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.psm.ricettasi.domain.model.RecipeWithAvailability
import it.unibo.psm.ricettasi.domain.repository.IngredientRepository
import it.unibo.psm.ricettasi.domain.repository.PantryRepository
import it.unibo.psm.ricettasi.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class SvuotaIlFrigoUiState(
    val isLoading: Boolean = true,
    // names of the pantry ingredients expiring within 3 days, shown in the info box
    val expiringNames: List<String> = emptyList(),
    val recipes: List<RecipeWithAvailability> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
)

/**
 * Drives the "Svuota il frigo" page: the chips of expiring ingredients come from the pantry,
 * while the recipe list is the remote search ranked by expiry urgency (order_by = expiring).
 */
class SvuotaIlFrigoViewModel(
    private val pantryRepository: PantryRepository,
    private val ingredientRepository: IngredientRepository,
    private val recipeRepository: RecipeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SvuotaIlFrigoUiState())
    val uiState: StateFlow<SvuotaIlFrigoUiState> = _uiState.asStateFlow()

    init {
        observeExpiringNames()
        observeFavoriteIds()
        loadRecipes()
    }

    private fun observeExpiringNames() {
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
                    .mapNotNull { byId[it.ingredientId]?.name }
                    .distinct()
            }.collect { names ->
                _uiState.update { it.copy(expiringNames = names) }
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

    private fun loadRecipes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { recipeRepository.emptyFridgeRecipes() }
                .onSuccess { recipes -> _uiState.update { it.copy(recipes = recipes) } }
            _uiState.update { it.copy(isLoading = false) }
        }
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
