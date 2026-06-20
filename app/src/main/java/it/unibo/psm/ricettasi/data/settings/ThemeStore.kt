package it.unibo.psm.ricettasi.data.settings

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Available theme modes for the settings screen. */
enum class ThemeOption(val label: String, val description: String) {
    LIGHT("Chiaro", "Sempre tema chiaro"),
    DARK("Scuro", "Sempre tema scuro"),
    AUTO("Automatico", "Segui il sistema"),
}

/**
 * Persists the user's theme preference to SharedPreferences
 * and exposes a [Flow] so the app shell can react to changes.
 */
class ThemeStore(context: Context) {

    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _theme = MutableStateFlow(current())
    val theme: Flow<ThemeOption> = _theme.asStateFlow()

    /** Reads the persisted theme, defaulting to [ThemeOption.AUTO]. */
    fun current(): ThemeOption {
        val stored = prefs.getString("theme", null)
        return ThemeOption.entries.firstOrNull { it.name == stored } ?: ThemeOption.AUTO
    }

    /** Persists [option] and pushes it to the reactive flow. */
    fun setTheme(option: ThemeOption) {
        prefs.edit { putString("theme", option.name) }
        _theme.value = option
    }
}