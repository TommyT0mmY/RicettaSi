package it.unibo.psm.ricettasi.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Risultato della RPC `search_recipes`.
 * Contiene i campi base della ricetta + i conteggi di disponibilità + tags.
 * Non include `steps` (JSONB) perché non servono nelle viste a lista.
 */
@Serializable
data class RecipeSearchResultDto(
    val id: String,
    val title: String,
    val description: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("preparation_time") val preparationTime: Int? = null,
    val difficulty: String = "facile",
    @SerialName("created_by_user_id") val createdByUserId: String? = null,
    @SerialName("available_count") val availableCount: Int = 0,
    @SerialName("required_count") val requiredCount: Int = 0,
    @SerialName("expiring_match_count") val expiringMatchCount: Int = 0,
    val categories: List<String> = emptyList(),
    @SerialName("meal_types") val mealTypes: List<String> = emptyList(),
)
