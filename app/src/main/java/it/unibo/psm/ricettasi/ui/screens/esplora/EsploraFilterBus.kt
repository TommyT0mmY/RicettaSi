package it.unibo.psm.ricettasi.ui.screens.esplora

import it.unibo.psm.ricettasi.domain.model.Difficulty
import it.unibo.psm.ricettasi.domain.model.MealType
import it.unibo.psm.ricettasi.domain.model.TimeWindow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Filters a Home "Vedi tutto" link wants Esplora to start with. Empty means "no filter". */
data class EsploraPreset(
    val mealType: MealType? = null,
    val timeWindow: TimeWindow? = null,
    val difficulties: Set<Difficulty> = emptySet(),
)

/**
 * Lets the Home screen hand a set of filters to the Esplora tab when a "Vedi tutto" is tapped.
 * Esplora keeps its ViewModel alive while you switch tabs, so a one-shot init read wouldn't catch
 * later taps; the ViewModel collects this flow instead and applies (then clears) whatever arrives.
 */
class EsploraFilterBus {
    private val _pending = MutableStateFlow<EsploraPreset?>(null)
    val pending: StateFlow<EsploraPreset?> = _pending.asStateFlow()

    fun request(preset: EsploraPreset) {
        _pending.value = preset
    }

    fun consume() {
        _pending.value = null
    }
}
