package it.unibo.psm.ricettasi.domain.model

/** Whether the user is currently logged in or not. */
enum class SessionState {
    /** Shown briefly on cold start while we check for a saved session. */
    LOADING,
    AUTHENTICATED,
    NOT_AUTHENTICATED,
}
