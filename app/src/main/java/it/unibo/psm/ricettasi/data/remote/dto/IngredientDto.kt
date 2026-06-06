package it.unibo.psm.ricettasi.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An ingredient name, either from the curated global ingredient list (kept in singular
 * form) or coined by a user themselves (not validated, can be written however they typed it).
 * A synonym points to its canonical entry through a self reference (parentIngredientId),
 * ingredients created by the user are marked as such.
 */
@Serializable
data class IngredientDto(
    val id: String,
    val name: String,
    @SerialName(COL_PARENT_INGREDIENT_ID) val parentIngredientId: String? = null,
    @SerialName(COL_CREATED_BY_USER) val createdByUser: Boolean = false,
    @SerialName(COL_USER_ID) val userId: String? = null,
) {
    companion object {
        const val COL_PARENT_INGREDIENT_ID = "parent_ingredient_id"
        const val COL_CREATED_BY_USER = "created_by_user"
        const val COL_USER_ID = "user_id"
    }
}
