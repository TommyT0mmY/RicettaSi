package it.unibo.psm.ricettasi.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Badge catalogue. The server `criteria` column (JSONB) is deliberately absent: the client
 * queries only `id, code, name, description` and remains unaware of the unlock criterion.
 */
@Serializable
data class BadgeDto(
    val id: String,
    val code: String,
    val name: String,
    val description: String? = null,
)

@Serializable
data class UserBadgeDto(
    @SerialName("user_id") val userId: String,
    @SerialName("badge_id") val badgeId: String,
    @SerialName("unlocked_date") val unlockedDate: String,
)
