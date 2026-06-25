package it.unibo.psm.ricettasi.data.sync

import kotlinx.coroutines.sync.Mutex

/**
 * A single mutex shared by the sync engine ([SyncManager]) and the sign-out cleanup
 * (`SessionRepository.signOut`). It is held while a refresh rewrites the local tables and while
 * sign-out wipes them, so the two can never interleave: without it, a refresh that is mid-flight
 * when the user logs out could repopulate the previous user's rows right after they were cleared.
 */
class SyncLock {
    val mutex = Mutex()
}
