package it.unibo.psm.ricettasi.ui.screens.pantry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.psm.ricettasi.domain.model.ExpiryStatus
import it.unibo.psm.ricettasi.domain.model.Ingredient
import it.unibo.psm.ricettasi.domain.model.PantryItem
import it.unibo.psm.ricettasi.domain.repository.IngredientRepository
import it.unibo.psm.ricettasi.domain.repository.PantryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

/**
 * A pantry item paired with its ingredient name, ready for display.
 * Kept as a simple data class so the composable doesn't need to look up names itself.
 */
data class PantryItemDisplay(
    val pantryItem: PantryItem,
    val ingredientName: String,
) {
    val expiryStatus: ExpiryStatus?
        get() = pantryItem.expiryStatus()

    val daysUntilExpiry: Long?
        get() {
            val expiry = pantryItem.expiryDate ?: return null
            return ChronoUnit.DAYS.between(LocalDate.now(), expiry)
        }
}

/**
 * State of the "Nuovo ingrediente" bottom sheet.
 */
data class AddIngredientState(
    val isVisible: Boolean = false,
    val nameQuery: String = "",
    val quantity: String = "",
    val expiryDate: LocalDate? = null,
    val suggestions: List<Ingredient> = emptyList(),
    val isSearching: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val selectedIngredient: Ingredient? = null,
)

/**
 * ViewModel for the Pantry screen.
 *
 * Observes the pantry and ingredient repositories reactively so the list stays
 * up to date across sync rounds. Manages the add-ingredient bottom sheet state
 * including the autocomplete search.
 */
class PantryViewModel(
    private val pantryRepository: PantryRepository,
    private val ingredientRepository: IngredientRepository,
) : ViewModel() {

    private val _activeItems = MutableStateFlow<List<PantryItemDisplay>>(emptyList())
    val activeItems: StateFlow<List<PantryItemDisplay>> = _activeItems.asStateFlow()

    private val _consumedItems = MutableStateFlow<List<PantryItemDisplay>>(emptyList())
    val consumedItems: StateFlow<List<PantryItemDisplay>> = _consumedItems.asStateFlow()

    private val _isFinishedExpanded = MutableStateFlow(false)
    val isFinishedExpanded: StateFlow<Boolean> = _isFinishedExpanded.asStateFlow()

    private val _addSheet = MutableStateFlow(AddIngredientState())
    val addSheet: StateFlow<AddIngredientState> = _addSheet.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            combine(
                pantryRepository.observeActive(),
                ingredientRepository.observeAll(),
            ) { items, ingredients ->
                val ingredientMap = ingredients.associateBy { it.id }
                items.map { item ->
                    val name = ingredientMap[item.ingredientId]?.name ?: "..."
                    PantryItemDisplay(pantryItem = item, ingredientName = name)
                }
            }.collect { _activeItems.value = it }
        }

        viewModelScope.launch {
            combine(
                pantryRepository.observeConsumed(),
                ingredientRepository.observeAll(),
            ) { items, ingredients ->
                val ingredientMap = ingredients.associateBy { it.id }
                items.map { item ->
                    val name = ingredientMap[item.ingredientId]?.name ?: "..."
                    PantryItemDisplay(pantryItem = item, ingredientName = name)
                }
            }.collect { _consumedItems.value = it }
        }
    }

    // --- Finished section accordion ---

    fun toggleFinishedExpanded() {
        _isFinishedExpanded.update { !it }
    }

    // --- Add ingredient sheet ---

    fun showAddSheet() {
        _addSheet.update { AddIngredientState(isVisible = true) }
    }

    fun hideAddSheet() {
        _addSheet.update { AddIngredientState() }
    }

    fun onNameQueryChanged(query: String) {
        _addSheet.update { it.copy(nameQuery = query, selectedIngredient = null, errorMessage = null) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _addSheet.update { it.copy(suggestions = emptyList(), isSearching = false) }
            return
        }
        searchJob = viewModelScope.launch {
            _addSheet.update { it.copy(isSearching = true) }
            try {
                val results = ingredientRepository.search(query, limit = 5)
                _addSheet.update { it.copy(suggestions = results, isSearching = false) }
            } catch (_: Exception) {
                _addSheet.update { it.copy(isSearching = false) }
            }
        }
    }

    fun onSuggestionSelected(ingredient: Ingredient) {
        _addSheet.update {
            it.copy(
                nameQuery = ingredient.name,
                selectedIngredient = ingredient,
                suggestions = emptyList(),
            )
        }
    }

    fun onQuantityChanged(quantity: String) {
        _addSheet.update { it.copy(quantity = quantity) }
    }

    fun onExpiryDateSelected(date: LocalDate) {
        _addSheet.update { it.copy(expiryDate = date) }
    }

    fun onClearExpiryDate() {
        _addSheet.update { it.copy(expiryDate = null) }
    }

    fun addIngredient() {
        val state = _addSheet.value
        if (state.isSubmitting || state.nameQuery.isBlank()) {
            if (state.nameQuery.isBlank()) {
                _addSheet.update { it.copy(errorMessage = "Dai un nome all'ingrediente per continuare.") }
            }
            return
        }

        viewModelScope.launch {
            _addSheet.update { it.copy(isSubmitting = true, errorMessage = null) }
            try {
                val ingredient = state.selectedIngredient
                    ?: ingredientRepository.createPersonal(state.nameQuery.trim())

                val item = PantryItem(
                    id = UUID.randomUUID().toString(),
                    ingredientId = ingredient.id,
                    quantity = state.quantity.ifBlank { null },
                    expiryDate = state.expiryDate,
                    addedDate = Instant.now(),
                )
                pantryRepository.add(item)
                _addSheet.update { AddIngredientState() }
            } catch (e: Exception) {
                _addSheet.update { it.copy(isSubmitting = false, errorMessage = e.message) }
            }
        }
    }

    // --- Toggle consumed / move to finished ---

    fun toggleConsumed(id: String, currentlyConsumed: Boolean) {
        viewModelScope.launch {
            try {
                pantryRepository.setConsumed(id, !currentlyConsumed)
            } catch (_: Exception) {
                // silently ignore - the local Room write already went through
            }
        }
    }

    // --- Delete ---

    /**
     * Hard-deletes the pantry item. The caller should offer undo via [restoreItem].
     */
    fun deleteItem(id: String) {
        viewModelScope.launch {
            try {
                pantryRepository.delete(id)
            } catch (_: Exception) {
                // silently ignore
            }
        }
    }

    /** Re-inserts an item that was just deleted (undo). */
    fun restoreItem(item: PantryItem) {
        viewModelScope.launch {
            try {
                pantryRepository.add(item)
            } catch (_: Exception) {
                // silently ignore
            }
        }
    }
}
