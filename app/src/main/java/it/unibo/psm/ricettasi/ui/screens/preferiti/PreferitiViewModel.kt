package it.unibo.psm.ricettasi.ui.screens.preferiti

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.psm.ricettasi.domain.model.Recipe
import it.unibo.psm.ricettasi.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PreferitiUiState(
    val isLoading: Boolean = true,
    val recipes: List<Recipe> = emptyList(),
)

class PreferitiViewModel(
    private val recipeRepository: RecipeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreferitiUiState())
    val uiState: StateFlow<PreferitiUiState> = _uiState.asStateFlow()

    init {
        observeFavorites()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            recipeRepository.observeFavorites().collect { recipes ->
                _uiState.update {
                    it.copy(isLoading = false, recipes = recipes)
                }
            }
        }
    }

    fun removeFavorite(recipeId: String) {
        viewModelScope.launch {
            try {
                recipeRepository.setFavorite(recipeId, false)
            } catch (_: Exception) {
                // silently ignore
            }
        }
    }
}
