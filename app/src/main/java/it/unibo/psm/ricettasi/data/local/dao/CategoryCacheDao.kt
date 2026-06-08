package it.unibo.psm.ricettasi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import it.unibo.psm.ricettasi.data.local.entity.CategoryCacheEntity

@Dao
interface CategoryCacheDao {

    @Query("SELECT * FROM category_cache")
    suspend fun getAll(): List<CategoryCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<CategoryCacheEntity>)

    @Query("DELETE FROM category_cache")
    suspend fun clear()
}
