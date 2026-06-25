package it.unibo.psm.ricettasi.data.mapper

import it.unibo.psm.ricettasi.data.local.dao.RecipeWithRelations
import it.unibo.psm.ricettasi.data.local.entity.RecipeCategoryEntity
import it.unibo.psm.ricettasi.data.local.entity.RecipeEntity
import it.unibo.psm.ricettasi.data.local.entity.RecipeIngredientEntity
import it.unibo.psm.ricettasi.data.local.entity.RecipeMealTypeEntity
import it.unibo.psm.ricettasi.data.remote.dto.RecipeDto
import it.unibo.psm.ricettasi.data.remote.dto.RecipeIngredientDto
import it.unibo.psm.ricettasi.data.remote.dto.RecipeSearchResultDto
import it.unibo.psm.ricettasi.domain.model.Difficulty
import it.unibo.psm.ricettasi.domain.model.MealType
import it.unibo.psm.ricettasi.domain.model.Recipe
import it.unibo.psm.ricettasi.domain.model.RecipeIngredient
import it.unibo.psm.ricettasi.domain.model.RecipeSummary
import it.unibo.psm.ricettasi.domain.model.RecipeWithAvailability
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

/** Json for (de)serializing the `steps` column in Room entities. */
private val recipeJson = Json { ignoreUnknownKeys = true }

// ---------- recipe ingredient ----------
fun RecipeIngredientDto.toDomain(): RecipeIngredient =
    RecipeIngredient(ingredientId = ingredientId, quantity = quantity, name = ingredient?.name)

fun RecipeIngredientEntity.toDomain(): RecipeIngredient =
    RecipeIngredient(ingredientId = ingredientId, quantity = quantity, name = null)

fun RecipeIngredient.toEntity(recipeId: String): RecipeIngredientEntity =
    RecipeIngredientEntity(recipeId = recipeId, ingredientId = ingredientId, quantity = quantity)

// ---------- meal type and category (relations -> domain lists) ----------
fun MealType.toEntity(recipeId: String): RecipeMealTypeEntity =
    RecipeMealTypeEntity(recipeId = recipeId, mealType = this)

fun String.toCategoryEntity(recipeId: String): RecipeCategoryEntity =
    RecipeCategoryEntity(recipeId = recipeId, category = this)

// ---------- recipe: DTO -> domain ----------
/**
 * Converts the DTO to a domain [Recipe]. [categoryNames] maps category ids to their
 * display names and is populated from the local [CategoryEntity] table; when a
 * category id isn't found the id itself is used as a fallback so the UI never shows
 * an empty chip.
 */
fun RecipeDto.toDomain(categoryNames: Map<String, String> = emptyMap()): Recipe = Recipe(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
    preparationTime = preparationTime,
    difficulty = Difficulty.fromValue(difficulty),
    steps = steps,
    ingredients = ingredients.map { it.toDomain() },
    mealTypes = mealTypes.mapNotNull { MealType.fromValue(it.mealType) },
    categories = categories.map { categoryNames[it.categoryId] ?: it.categoryId },
    createdByUserId = createdByUserId,
    servings = servings,
)

/** Per casi in cui le relazioni sono fornite separatamente (es. conversione da entity Room). */
fun RecipeDto.toDomain(
    ingredients: List<RecipeIngredient>,
    mealTypes: List<MealType>,
    categories: List<String>,
): Recipe = Recipe(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
    preparationTime = preparationTime,
    difficulty = Difficulty.fromValue(difficulty),
    steps = steps,
    ingredients = ingredients,
    mealTypes = mealTypes,
    categories = categories,
    createdByUserId = createdByUserId,
    servings = servings,
)

// ---------- recipe: Entity <-> domain ----------
fun RecipeEntity.toDomain(
    ingredients: List<RecipeIngredient> = emptyList(),
    mealTypes: List<MealType> = emptyList(),
    categories: List<String> = emptyList(),
): Recipe = Recipe(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
    preparationTime = preparationTime,
    difficulty = difficulty,
    steps = recipeJson.decodeFromString(steps),
    ingredients = ingredients,
    mealTypes = mealTypes,
    categories = categories,
    createdByUserId = createdByUserId,
    servings = servings,
)

/** Cached recipe + Room relations -> domain. */
fun RecipeWithRelations.toDomain(): Recipe = recipe.toDomain(
    ingredients = ingredients.map { it.toDomain() },
    mealTypes = mealTypes.map { it.mealType },
    categories = categories.map { it.category },
)

// ---------- full recipe -> entity relations (for the local cache) ----------

/** Explodes a domain [Recipe] into the Room entities for its relations (for the local cache). */
fun Recipe.toIngredientEntities(): List<RecipeIngredientEntity> = ingredients.map { it.toEntity(id) }
fun Recipe.toMealTypeEntities(): List<RecipeMealTypeEntity> = mealTypes.map { it.toEntity(id) }
fun Recipe.toCategoryEntities(): List<RecipeCategoryEntity> = categories.map { it.toCategoryEntity(id) }

// ---------- search result (RPC search_recipes) -> domain ----------
fun RecipeSearchResultDto.toDomain(): RecipeWithAvailability = RecipeWithAvailability(
    recipe = RecipeSummary(
        id = id,
        title = title,
        imageUrl = imageUrl,
        preparationTime = preparationTime,
        difficulty = Difficulty.fromValue(difficulty),
        mealTypes = mealTypes.mapNotNull { MealType.fromValue(it) },
        categories = categories,
    ),
    availableCount = availableCount,
    requiredCount = requiredCount,
    expiringMatchCount = expiringMatchCount,
)

fun Recipe.toEntity(cachedAt: Instant = Instant.now()): RecipeEntity = RecipeEntity(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
    preparationTime = preparationTime,
    difficulty = difficulty,
    steps = recipeJson.encodeToString(steps),
    createdByUserId = createdByUserId,
    servings = servings,
    cachedAt = cachedAt.toEpochMilli(),
)
