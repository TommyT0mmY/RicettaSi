package it.unibo.psm.ricettasi.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A recipe in compact form, exactly what the `search_recipes` RPC returns for a list row.
 * No description, no servings, no author, just enough to draw a card, plus the availability
 * counts worked out against the user's pantry (how many of its ingredients are available, how
 * many are required and how many are about to expire) and its tags.
 */
@Serializable
data class RecipeSearchResultDto(
    val id: String,
    val title: String,
    @SerialName(COL_IMAGE_URL) val imageUrl: String? = null,
    @SerialName(COL_PREPARATION_TIME) val preparationTime: Int? = null,
    val difficulty: String = "facile",
    @SerialName(COL_AVAILABLE_COUNT) val availableCount: Int = 0,
    @SerialName(COL_REQUIRED_COUNT) val requiredCount: Int = 0,
    @SerialName(COL_EXPIRING_MATCH_COUNT) val expiringMatchCount: Int = 0,
    val categories: List<String> = emptyList(),
    @SerialName(COL_MEAL_TYPES) val mealTypes: List<String> = emptyList(),
) {
    companion object {
        const val COL_IMAGE_URL = "image_url"
        const val COL_PREPARATION_TIME = "preparation_time"
        const val COL_AVAILABLE_COUNT = "available_count"
        const val COL_REQUIRED_COUNT = "required_count"
        const val COL_EXPIRING_MATCH_COUNT = "expiring_match_count"
        const val COL_MEAL_TYPES = "meal_types"
    }
}
