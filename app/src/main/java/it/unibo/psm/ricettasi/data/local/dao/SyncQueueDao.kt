package it.unibo.psm.ricettasi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.unibo.psm.ricettasi.data.local.entity.SyncQueueEntity

@Dao
interface SyncQueueDao {

    @Insert
    suspend fun insert(op: SyncQueueEntity)

    @Query("SELECT * FROM sync_queue ORDER BY clientTimestamp ASC")
    suspend fun getPending(): List<SyncQueueEntity>

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM sync_queue")
    suspend fun clear()
}
