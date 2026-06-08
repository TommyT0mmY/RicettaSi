package it.unibo.psm.ricettasi.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.unibo.psm.ricettasi.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Query("SELECT * FROM user_profiles LIMIT 1")
    fun observeProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    suspend fun getById(userId: String): UserProfileEntity?

    @Upsert
    suspend fun upsert(profile: UserProfileEntity)

    @Query("DELETE FROM user_profiles")
    suspend fun clear()
}
