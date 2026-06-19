package it.unibo.psm.ricettasi.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.unibo.psm.ricettasi.data.sync.SyncWorker
import it.unibo.psm.ricettasi.domain.model.SessionState
import it.unibo.psm.ricettasi.domain.repository.SessionRepository
import it.unibo.psm.ricettasi.ui.screens.auth.AuthRoute
import org.koin.compose.koinInject

/**
 * Root gate: reads the session state and decides what to display.
 */
@Composable
fun RootGate() {
    val sessionRepository: SessionRepository = koinInject()
    val sessionState by sessionRepository.sessionState.collectAsStateWithLifecycle(
        initialValue = SessionState.LOADING,
    )

    when (sessionState) {
        SessionState.LOADING -> LoadingScreen()
        SessionState.NOT_AUTHENTICATED -> AuthRoute()
        SessionState.AUTHENTICATED -> {
            // Pull the user's data as soon as the session is available. The regular triggers
            // (onResume, periodic work, network recovery) do not cover a fresh login, since the
            // activity is already resumed when the user signs in.
            val context = LocalContext.current
            LaunchedEffect(Unit) { SyncWorker.triggerNow(context) }
            RicettaSiNavHost()
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}