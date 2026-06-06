package it.unibo.psm.ricettasi.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A single ingredient kept in a user's pantry. Quantity and expiry are optional and
 * only informational; the consumed flag, together with its dates, records whether the
 * item has been used up.
 */
@Serializable
data class PantryItemDto(
    val id: String,
    @SerialName(COL_USER_ID) val userId: String,
    @SerialName(COL_INGREDIENT_ID) val ingredientId: String,
    val quantity: String? = null,
    /** `yyyy-MM-dd` */
    @SerialName(COL_EXPIRY_DATE) val expiryDate: String? = null,
    /** timestamptz */
    @SerialName(COL_ADDED_DATE) val addedDate: String,
    val consumed: Boolean = false,
    /** timestamptz */
    @SerialName(COL_CONSUMED_DATE) val consumedDate: String? = null,
) {
    companion object {
        const val COL_ID = "id"
        const val COL_USER_ID = "user_id"
        const val COL_INGREDIENT_ID = "ingredient_id"
        const val COL_EXPIRY_DATE = "expiry_date"
        const val COL_ADDED_DATE = "added_date"
        const val COL_CONSUMED_DATE = "consumed_date"
    }
}
