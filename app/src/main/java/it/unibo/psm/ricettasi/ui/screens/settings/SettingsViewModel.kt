package it.unibo.psm.ricettasi.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.psm.ricettasi.data.settings.ThemeOption
import it.unibo.psm.ricettasi.data.settings.ThemeStore
import it.unibo.psm.ricettasi.domain.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Settings screen.
 *
 * Holds the currently selected theme from [ThemeStore] and handles sign-out
 * through [SessionRepository].
 */
class SettingsViewModel(
    private val themeStore: ThemeStore,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _selectedTheme = MutableStateFlow(themeStore.current())
    val selectedTheme: StateFlow<ThemeOption> = _selectedTheme.asStateFlow()

    /** Updates the theme both in-memory and on disk. */
    fun setTheme(option: ThemeOption) {
        themeStore.setTheme(option)
        _selectedTheme.value = option
    }

    /** Signs the user out on a background dispatcher. */
    fun signOut() {
        viewModelScope.launch(Dispatchers.IO) {
            sessionRepository.signOut()
        }
    }
}