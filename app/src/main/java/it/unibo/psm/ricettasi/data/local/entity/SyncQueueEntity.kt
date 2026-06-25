package it.unibo.psm.ricettasi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import it.unibo.psm.ricettasi.data.remote.dto.SyncPayload
import it.unibo.psm.ricettasi.data.sync.SyncAction

/**
 * Local queue of writes made while offline, drained to Supabase in FIFO order when connectivity
 * returns. Purely local table: it has no server counterpart or DTO.
 */
@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val id: String,
    val action: SyncAction,
    /** The DTO to push. Room keeps it as JSON through a type converter. */
    val payload: SyncPayload,
    /** Device clock at the time of the action (epoch millis). */
    val clientTimestamp: Long,
)
