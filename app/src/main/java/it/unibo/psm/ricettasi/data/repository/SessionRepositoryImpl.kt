package it.unibo.psm.ricettasi.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import it.unibo.psm.ricettasi.data.local.RicettaSiDatabase
import it.unibo.psm.ricettasi.data.sync.SyncVersionStore
import it.unibo.psm.ricettasi.domain.model.SessionState
import it.unibo.psm.ricettasi.domain.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SessionRepositoryImpl(
    private val client: SupabaseClient,
    private val database: RicettaSiDatabase,
    private val versionStore: SyncVersionStore,
) : SessionRepository {

    override val sessionState: Flow<SessionState> =
        client.auth.sessionStatus.map { status ->
            when (status) {
                is SessionStatus.Authenticated -> SessionState.AUTHENTICATED
                is SessionStatus.NotAuthenticated -> SessionState.NOT_AUTHENTICATED
                is SessionStatus.RefreshFailure -> SessionState.NOT_AUTHENTICATED
                is SessionStatus.Initializing -> SessionState.LOADING
            }
        }

    override fun currentUserId(): String? = client.auth.currentUserOrNull()?.id

    override suspend fun signIn(email: String, password: String) {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signUp(email: String, password: String) {
        client.auth.signUpWith(Email) { // It returns null because we disabled email confirmation
            this.email = email
            this.password = password
        }
    }

    override suspend fun signOut() {
        // Local cleanup must run regardless: if the network call fails but the local session
        // is already invalidated, leaving the previous user's data behind would be a data leak.
        try {
            client.auth.signOut()
        } finally {
            withContext(Dispatchers.IO) { database.clearAllTables() }
            versionStore.clear()
        }
    }
}
