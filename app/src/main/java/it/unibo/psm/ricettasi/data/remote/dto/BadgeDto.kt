package it.unibo.psm.ricettasi.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A gamification badge. Unlock criteria live server-side, [description] is the only hint shown to the user. */
@Serializable
data class BadgeDto(
    val id: String,
    val code: String,
    val name: String,
    val description: String? = null,
) {
    companion object {
        const val TABLE = "badges"
        const val COL_ID = "id"
        const val COL_CODE = "code"
        const val COL_NAME = "name"
        const val COL_DESCRIPTION = "description"
    }
}

/** Ties a user to a badge they've unlocked, with the date it happened. */
@Serializable
data class UserBadgeDto(
    @SerialName(COL_USER_ID) val userId: String,
    @SerialName(COL_BADGE_ID) val badgeId: String,
    @SerialName(COL_UNLOCKED_DATE) val unlockedDate: String,
) {
    companion object {
        const val TABLE = "user_badges"
        const val COL_USER_ID = "user_id"
        const val COL_BADGE_ID = "badge_id"
        const val COL_UNLOCKED_DATE = "unlocked_date"
    }
}
