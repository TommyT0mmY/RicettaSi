package it.unibo.psm.ricettasi.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.unibo.psm.ricettasi.data.local.entity.PantryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryDao {

    // expiryDate is ISO `yyyy-MM-dd` -> lexicographic order matches chronological order.
    @Query(
        "SELECT * FROM pantry_items WHERE consumed = 0 " +
            "ORDER BY (expiryDate IS NULL), expiryDate ASC, addedDate DESC",
    )
    fun observeActive(): Flow<List<PantryItemEntity>>

    @Query("SELECT * FROM pantry_items WHERE consumed = 1 ORDER BY consumedDate DESC")
    fun observeConsumed(): Flow<List<PantryItemEntity>>

    @Query("SELECT * FROM pantry_items WHERE consumed = 0")
    suspend fun getActive(): List<PantryItemEntity>

    @Query("SELECT * FROM pantry_items WHERE id = :id")
    suspend fun getById(id: String): PantryItemEntity?

    @Upsert
    suspend fun upsert(item: PantryItemEntity)

    @Upsert
    suspend fun upsertAll(items: List<PantryItemEntity>)

    @Query("UPDATE pantry_items SET consumed = :consumed, consumedDate = :consumedDate WHERE id = :id")
    suspend fun setConsumed(id: String, consumed: Boolean, consumedDate: Long?)

    @Query("DELETE FROM pantry_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM pantry_items")
    suspend fun clear()
}
