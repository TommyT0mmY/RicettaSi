package it.unibo.psm.ricettasi.ui.screens.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.psm.ricettasi.domain.model.Recipe
import it.unibo.psm.ricettasi.domain.repository.IngredientRepository
import it.unibo.psm.ricettasi.domain.repository.RecipeRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecipeDetailUiState(
    val recipe: Recipe? = null,
    val isFavorite: Boolean = false,
    val isCooked: Boolean = false,
    /** Maps ingredientId -> display name, resolved from the ingredient table. */
    val ingredientNames: Map<String, String> = emptyMap(),
    /** Recipe ingredient ids that the user currently has in the pantry. */
    val availableIngredientIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class RecipeDetailViewModel(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    private var currentRecipeId: String? = null

    /**
     * Loads the full recipe from remote (with local-cache fallback), resolves ingredient
     * names from their ids, and starts observing the favourite state for this recipe.
     *
     * Idempotent: calling [init] again with the same id is a no-op.
     */
    fun init(recipeId: String) {
        if (recipeId == currentRecipeId) return
        currentRecipeId = recipeId

        _uiState.update { it.copy(isLoading = true, errorMessage = null, recipe = null, isCooked = false) }

        viewModelScope.launch {
            val recipe = recipeRepository.loadAndCacheRecipe(recipeId)
            if (recipe == null) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Ricetta non trovata")
                }
                return@launch
            }

            // Resolve ingredient names in parallel
            val names = recipe.ingredients
                .map { it.ingredientId }
                .distinct()
                .let { ids ->
                    ids.map { id ->
                        async {
                            val ingredient = ingredientRepository.getById(id)
                            id to (ingredient?.name ?: id)
                        }
                    }
                }
                .let { deferreds -> deferreds.map { it.await() } }
                .toMap()

            _uiState.update {
                it.copy(
                    recipe = recipe,
                    ingredientNames = names,
                    availableIngredientIds = recipeRepository.availableIngredientIds(recipeId),
                    isLoading = false,
                )
            }
        }

        // Reactive favourite observation (separate coroutine, lives for the ViewModel lifetime)
        viewModelScope.launch {
            recipeRepository.observeIsFavorite(recipeId).collect { fav ->
                _uiState.update { it.copy(isFavorite = fav) }
            }
        }
    }

    fun toggleFavorite() {
        val id = currentRecipeId ?: return
        viewModelScope.launch {
            recipeRepository.setFavorite(id, !_uiState.value.isFavorite)
        }
    }

    fun markCooked() {
        val id = currentRecipeId ?: return
        viewModelScope.launch {
            recipeRepository.recordCooked(id)
            _uiState.update { it.copy(isCooked = true) }
        }
    }
}