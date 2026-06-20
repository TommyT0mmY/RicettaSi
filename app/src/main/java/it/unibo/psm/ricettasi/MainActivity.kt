package it.unibo.psm.ricettasi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import it.unibo.psm.ricettasi.data.settings.ThemeOption
import it.unibo.psm.ricettasi.data.settings.ThemeStore
import it.unibo.psm.ricettasi.data.sync.SyncWorker
import it.unibo.psm.ricettasi.ui.navigation.RootGate
import it.unibo.psm.ricettasi.ui.theme.RicettaSiTheme
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SyncWorker.schedule(this)
        setContent {
            val themeStore: ThemeStore = koinInject()
            val theme by themeStore.theme.collectAsStateWithLifecycle(
                initialValue = ThemeOption.AUTO,
            )
            RicettaSiTheme(
                darkTheme = when (theme) {
                    ThemeOption.LIGHT -> false
                    ThemeOption.DARK -> true
                    ThemeOption.AUTO -> isSystemInDarkTheme()
                },
            ) {
                RootGate()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SyncWorker.triggerNow(this)
    }
}