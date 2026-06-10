package it.unibo.psm.ricettasi.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        SessionState.AUTHENTICATED -> RicettaSiNavHost()
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