package it.unibo.psm.ricettasi.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Full recipe with all its relations embedded (PostgREST select with joins).
 * Every M2M relation picks columns directly from the junction table, never
 * nesting through foreign keys, so the DTO shapes stay flat and consistent.
 */
@Serializable
data class RecipeDto(
    val id: String,
    val title: String,
    val description: String? = null,
    @SerialName(COL_IMAGE_URL) val imageUrl: String? = null,
    @SerialName(COL_PREPARATION_TIME) val preparationTime: Int? = null,
    val difficulty: String = "facile",
    val servings: String? = null,
    val steps: List<String> = emptyList(), // JSONB with ordered instruction strings
    @SerialName(COL_CREATED_BY_USER_ID) val createdByUserId: String? = null,
    @SerialName(RecipeIngredientDto.TABLE) val ingredients: List<RecipeIngredientDto> = emptyList(),
    @SerialName(RecipeMealTypeDto.TABLE) val mealTypes: List<RecipeMealTypeDto> = emptyList(),
    @SerialName(RecipeCategoryDto.TABLE) val categories: List<RecipeCategoryDto> = emptyList(),
) {
    companion object {
        const val TABLE = "recipes"
        const val COL_ID = "id"
        const val COL_IMAGE_URL = "image_url"
        const val COL_PREPARATION_TIME = "preparation_time"
        const val COL_CREATED_BY_USER_ID = "created_by_user_id"
    }
}

/** Ties a recipe to an ingredient it needs, with an optional free-text quantity. */
@Serializable
data class RecipeIngredientDto(
    @SerialName(COL_INGREDIENT_ID) val ingredientId: String,
    val quantity: String? = null,
    /** Embedded via PostgREST resource embedding (`ingredients(name)`). */
    @SerialName("ingredients") val ingredient: IngredientNameEmbed? = null,
) {
    /** Shape of the embedded `ingredients` row. */
    @Serializable
    data class IngredientNameEmbed(val name: String)

    companion object {
        const val TABLE = "recipe_ingredients"
        const val COL_INGREDIENT_ID = "ingredient_id"
        const val COL_QUANTITY = "quantity"
    }
}

/** Ties a recipe to a meal type. */
@Serializable
data class RecipeMealTypeDto(
    @SerialName(COL_MEAL_TYPE) val mealType: String, // Enum value directly, no id (e.g. 'pranzo')
) {
    companion object {
        const val TABLE = "recipe_meal_types"
        const val COL_MEAL_TYPE = "meal_type"
    }
}

/** Ties a recipe to a category by referencing the category id directly. */
@Serializable
data class RecipeCategoryDto(
    @SerialName(COL_CATEGORY_ID) val categoryId: String,
) {
    companion object {
        const val TABLE = "recipe_categories"
        const val COL_CATEGORY_ID = "category_id"
    }
}
