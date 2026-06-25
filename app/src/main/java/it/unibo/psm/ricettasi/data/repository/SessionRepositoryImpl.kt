package it.unibo.psm.ricettasi.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import it.unibo.psm.ricettasi.data.local.RicettaSiDatabase
import it.unibo.psm.ricettasi.data.sync.SyncLock
import it.unibo.psm.ricettasi.data.sync.SyncVersionStore
import it.unibo.psm.ricettasi.domain.model.SessionState
import it.unibo.psm.ricettasi.domain.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class SessionRepositoryImpl(
    private val client: SupabaseClient,
    private val database: RicettaSiDatabase,
    private val versionStore: SyncVersionStore,
    private val syncLock: SyncLock,
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
            // Take the same lock the sync uses, so the wipe cannot interleave with a refresh that
            // is mid-flight: otherwise that refresh could repopulate the previous user's rows right
            // after they were cleared. The session is already invalidated above, so any sync that
            // runs next finds no user and does nothing.
            syncLock.mutex.withLock {
                withContext(Dispatchers.IO) { database.clearAllTables() }
                versionStore.clear()
            }
        }
    }
}
