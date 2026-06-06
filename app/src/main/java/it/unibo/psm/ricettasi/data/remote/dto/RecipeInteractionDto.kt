package it.unibo.psm.ricettasi.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Ties a user to a recipe they've saved as favourite, with the date it happened. */
@Serializable
data class FavoriteDto(
    @SerialName(COL_USER_ID) val userId: String,
    @SerialName(COL_RECIPE_ID) val recipeId: String,
    @SerialName(COL_SAVED_DATE) val savedDate: String,
) {
    companion object {
        const val COL_USER_ID = "user_id"
        const val COL_RECIPE_ID = "recipe_id"
        const val COL_SAVED_DATE = "saved_date"
    }
}

/** A record that a user has cooked a recipe, with the date it was cooked. */
@Serializable
data class CookedRecipeDto(
    val id: String,
    @SerialName(COL_USER_ID) val userId: String,
    @SerialName(COL_RECIPE_ID) val recipeId: String,
    @SerialName(COL_COOKED_DATE) val cookedDate: String,
) {
    companion object {
        const val COL_USER_ID = "user_id"
        const val COL_RECIPE_ID = "recipe_id"
        const val COL_COOKED_DATE = "cooked_date"
    }
}

