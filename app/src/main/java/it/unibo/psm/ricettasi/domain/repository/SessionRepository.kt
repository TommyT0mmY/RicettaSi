package it.unibo.psm.ricettasi.domain.repository

import it.unibo.psm.ricettasi.domain.model.SessionState
import kotlinx.coroutines.flow.Flow

/** Current user session state (based on Supabase Auth). */
interface SessionRepository {

    /** Observable session state, used by the navigation gate. */
    val sessionState: Flow<SessionState>

    /** Id of the authenticated user, or `null` if not logged in. */
    fun currentUserId(): String?

    /** Signs in with email/password. Throws on wrong credentials or no network. */
    suspend fun signIn(email: String, password: String)

    /**
     * Registers a new user with email/password. Email confirmation is disabled, so the session is
     * established immediately upon registration; a subsequent [signIn] call is not required.
     */
    suspend fun signUp(email: String, password: String)

    /**
     * Ends the current session and clears all locally cached data, since the local database
     * holds a single user's data at a time.
     */
    suspend fun signOut()
}
