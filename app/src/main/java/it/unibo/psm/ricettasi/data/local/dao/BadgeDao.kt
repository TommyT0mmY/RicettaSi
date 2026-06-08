package it.unibo.psm.ricettasi.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.unibo.psm.ricettasi.data.local.entity.BadgeEntity
import it.unibo.psm.ricettasi.data.local.entity.UserBadgeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BadgeDao {

    @Query("SELECT * FROM badges ORDER BY name")
    fun observeBadges(): Flow<List<BadgeEntity>>

    @Query("SELECT * FROM user_badges")
    fun observeUserBadges(): Flow<List<UserBadgeEntity>>

    @Upsert
    suspend fun upsertBadges(items: List<BadgeEntity>)

    @Upsert
    suspend fun upsertUserBadges(items: List<UserBadgeEntity>)

    @Query("DELETE FROM user_badges")
    suspend fun clearUserBadges()
}
