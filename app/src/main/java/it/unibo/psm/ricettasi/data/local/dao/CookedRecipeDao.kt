package it.unibo.psm.ricettasi.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.unibo.psm.ricettasi.data.local.entity.CookedRecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CookedRecipeDao {

    @Query("SELECT * FROM cooked_recipes ORDER BY cookedDate DESC")
    fun observeAll(): Flow<List<CookedRecipeEntity>>

    @Query("SELECT COUNT(*) FROM cooked_recipes")
    fun observeCount(): Flow<Int>

    @Upsert
    suspend fun upsert(item: CookedRecipeEntity)

    @Upsert
    suspend fun upsertAll(items: List<CookedRecipeEntity>)

    @Query("DELETE FROM cooked_recipes")
    suspend fun clear()
}
