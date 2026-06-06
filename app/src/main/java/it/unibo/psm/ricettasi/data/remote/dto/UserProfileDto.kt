package it.unibo.psm.ricettasi.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileDto(
    @SerialName("user_id") val userId: String,
    @SerialName("display_name") val displayName: String = "",
    @SerialName("avatar_emoji") val avatarEmoji: String = "👨‍🍳",
    val xp: Int = 0,
    val level: Int = 1,
    /** Sync counters (read-only on the client side). */
    @SerialName("ingredients_version") val ingredientsVersion: Long = 0,
    @SerialName("data_version") val dataVersion: Long = 0,
)
