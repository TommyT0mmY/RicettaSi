package it.unibo.psm.ricettasi.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Ricetta con tutte le relazioni (opzionali, presenti solo quando la select
 * di PostgREST include l'embedding). Usato sia per liste (search, suggerimenti)
 * che per il dettaglio.
 */
@Serializable
data class RecipeDto(
    val id: String,
    val title: String,
    val description: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("preparation_time") val preparationTime: Int? = null,
    val difficulty: String = "facile",
    /** Colonna JSONB. */
    val steps: List<RecipeStepDto> = emptyList(),
    @SerialName("created_by_user_id") val createdByUserId: String? = null,
    @SerialName("recipe_ingredients") val ingredients: List<RecipeIngredientDto> = emptyList(),
    @SerialName("recipe_meal_types") val mealTypes: List<RecipeMealTypeDto> = emptyList(),
    @SerialName("recipe_categories") val categories: List<RecipeCategoryDto> = emptyList(),
)

@Serializable
data class RecipeStepDto(
    @SerialName("step_number") val stepNumber: Int,
    val instruction: String,
)

@Serializable
data class RecipeIngredientDto(
    @SerialName("recipe_id") val recipeId: String,
    @SerialName("ingredient_id") val ingredientId: String,
    val quantity: String? = null,
    val optional: Boolean = false,
)

@Serializable
data class RecipeMealTypeDto(
    @SerialName("recipe_id") val recipeId: String,
    @SerialName("meal_type") val mealType: String,
)

@Serializable
data class RecipeCategoryDto(
    @SerialName("recipe_id") val recipeId: String,
    val category: String,
)
