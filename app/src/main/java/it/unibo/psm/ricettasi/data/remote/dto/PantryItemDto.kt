package it.unibo.psm.ricettasi.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PantryItemDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("ingredient_id") val ingredientId: String,
    val quantity: String? = null,
    /** ISO `yyyy-MM-dd`. */
    @SerialName("expiry_date") val expiryDate: String? = null,
    /** ISO timestamptz. */
    @SerialName("added_date") val addedDate: String,
    val consumed: Boolean = false,
    /** ISO timestamptz. */
    @SerialName("consumed_date") val consumedDate: String? = null,
)
