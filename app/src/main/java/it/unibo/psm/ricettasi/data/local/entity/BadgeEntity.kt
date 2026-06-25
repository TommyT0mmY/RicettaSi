package it.unibo.psm.ricettasi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "badges")
data class BadgeEntity(
    @PrimaryKey val id: String,
    val code: String,
    val name: String,
    val description: String?,
)

/** Badge unlock record for the current user. */
@Entity(tableName = "user_badges")
data class UserBadgeEntity(
    @PrimaryKey val badgeId: String,
    /** Epoch millis. */
    val unlockedDate: Long,
)
