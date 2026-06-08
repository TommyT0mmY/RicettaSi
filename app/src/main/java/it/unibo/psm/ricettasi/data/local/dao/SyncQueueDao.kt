package it.unibo.psm.ricettasi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.unibo.psm.ricettasi.data.local.entity.SyncQueueEntity

@Dao
interface SyncQueueDao {

    @Insert
    suspend fun insert(op: SyncQueueEntity)

    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY clientTimestamp ASC")
    suspend fun getPending(): List<SyncQueueEntity>

    @Query("UPDATE sync_queue SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM sync_queue WHERE status = 'SYNCED'")
    suspend fun deleteSynced()

    @Query("DELETE FROM sync_queue")
    suspend fun clear()
}
