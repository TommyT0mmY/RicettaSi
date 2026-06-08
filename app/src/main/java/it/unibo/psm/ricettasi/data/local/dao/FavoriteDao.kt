package it.unibo.psm.ricettasi.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.unibo.psm.ricettasi.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites ORDER BY savedDate DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT COUNT(*) FROM favorites")
    fun observeCount(): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE recipeId = :recipeId)")
    fun observeIsFavorite(recipeId: String): Flow<Boolean>

    @Upsert
    suspend fun upsert(item: FavoriteEntity)

    @Upsert
    suspend fun upsertAll(items: List<FavoriteEntity>)

    @Query("DELETE FROM favorites WHERE recipeId = :recipeId")
    suspend fun deleteById(recipeId: String)

    @Query("DELETE FROM favorites")
    suspend fun clear()
}
