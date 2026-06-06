package it.unibo.psm.ricettasi.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IngredientDto(
    val id: String,
    val name: String,
    @SerialName("parent_ingredient_id") val parentIngredientId: String? = null,
    @SerialName("created_by_user") val createdByUser: Boolean = false,
    @SerialName("user_id") val userId: String? = null,
)
