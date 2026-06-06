package it.unibo.psm.ricettasi.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A user's profile and gamification state.
 * The remote row also has sync version columns, but those aren't decoded here
 * since the app reads them from get_sync_versions instead.
 */
@Serializable
data class UserProfileDto(
    @SerialName(COL_USER_ID) val userId: String,
    @SerialName(COL_DISPLAY_NAME) val displayName: String = "",
    @SerialName(COL_AVATAR_EMOJI) val avatarEmoji: String = "👨‍🍳",
    val xp: Int = 0,
    val level: Int = 1,
) {
    companion object {
        const val COL_USER_ID = "user_id"
        const val COL_DISPLAY_NAME = "display_name"
        const val COL_AVATAR_EMOJI = "avatar_emoji"
    }
}
