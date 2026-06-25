package it.unibo.psm.ricettasi.data.sync

import androidx.room.withTransaction
import it.unibo.psm.ricettasi.data.local.RicettaSiDatabase
import it.unibo.psm.ricettasi.data.local.dao.SyncQueueDao
import it.unibo.psm.ricettasi.data.local.entity.SyncQueueEntity
import it.unibo.psm.ricettasi.data.remote.dto.SyncPayload
import java.util.UUID

/**
 * Single entry point for offline-first writes. [write] changes the local Room table the UI observes
 * and records the same change in the sync queue, both in one transaction so they can never get out
 * of step. The change reaches Supabase later by draining the queue: [SyncManager.pushPending] tries
 * it right away (a no-op when offline, the row just waits for the next sync). Since the push always
 * goes through the queue there is a single write path, no online/offline fork to keep in sync.
 */
class SyncWriter(
    private val db: RicettaSiDatabase,
    private val syncQueue: SyncQueueDao,
    private val syncManager: SyncManager,
) {
    suspend fun write(action: SyncAction, payload: SyncPayload, localChange: suspend () -> Unit) {
        db.withTransaction {
            localChange()
            syncQueue.insert(
                SyncQueueEntity(
                    id = UUID.randomUUID().toString(),
                    action = action,
                    payload = payload,
                    clientTimestamp = System.currentTimeMillis(),
                ),
            )
        }
        syncManager.pushPending()
    }
}
