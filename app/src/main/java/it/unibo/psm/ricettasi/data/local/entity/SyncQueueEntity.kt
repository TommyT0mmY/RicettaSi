package it.unibo.psm.ricettasi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local queue of writes made while offline, drained to Supabase in FIFO order when connectivity
 * returns. Purely local table: it has no server counterpart or DTO.
 */
@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val id: String,
    /** Table name: "pantry_items", "favorites", ... */
    val entity: String,
    /** "INSERT" | "UPDATE" | "DELETE". */
    val action: String,
    /** Full operation row, serialised as JSON. */
    val payload: String,
    /** Device clock at the time of the action (epoch millis). */
    val clientTimestamp: Long,
    /** "PENDING" | "SYNCED". */
    val status: String,
)
