package it.unibo.psm.ricettasi.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Recipe in the local cache (favourites). Recipes are never downloaded all at once:
 * this table holds only the cached subset. [cachedAt] is used for TTL.
 */
@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    /** Minutes. */
    val preparationTime: Int?,
    /** Persisted form of [it.unibo.psm.ricettasi.domain.model.Difficulty]. */
    val difficulty: String,
    /** JSON: ordered array of instruction strings. */
    val steps: String,
    /** Free-text yield: "4 persone", "20-25 biscotti", "8 fette", ecc. */
    val servings: String?,
    val createdByUserId: String?,
    /** Epoch millis of the local caching timestamp (for TTL). */
    val cachedAt: Long,
)

@Entity(
    tableName = "recipe_ingredients",
    primaryKeys = ["recipeId", "ingredientId"],
    indices = [Index("ingredientId")],
)
data class RecipeIngredientEntity(
    val recipeId: String,
    val ingredientId: String,
    val quantity: String?,
)

@Entity(
    tableName = "recipe_meal_types",
    primaryKeys = ["recipeId", "mealType"],
)
data class RecipeMealTypeEntity(
    val recipeId: String,
    val mealType: String,
)

@Entity(
    tableName = "recipe_categories",
    primaryKeys = ["recipeId", "category"],
)
data class RecipeCategoryEntity(
    val recipeId: String,
    val category: String,
)
