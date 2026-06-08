package it.unibo.psm.ricettasi.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Upsert
import it.unibo.psm.ricettasi.data.local.entity.RecipeCategoryEntity
import it.unibo.psm.ricettasi.data.local.entity.RecipeEntity
import it.unibo.psm.ricettasi.data.local.entity.RecipeIngredientEntity
import it.unibo.psm.ricettasi.data.local.entity.RecipeMealTypeEntity
import kotlinx.coroutines.flow.Flow

/** Recipe with its relations, assembled by Room. */
data class RecipeWithRelations(
    @Embedded val recipe: RecipeEntity,
    @Relation(parentColumn = "id", entityColumn = "recipeId")
    val ingredients: List<RecipeIngredientEntity>,
    @Relation(parentColumn = "id", entityColumn = "recipeId")
    val mealTypes: List<RecipeMealTypeEntity>,
    @Relation(parentColumn = "id", entityColumn = "recipeId")
    val categories: List<RecipeCategoryEntity>,
)

@Dao
interface RecipeDao {

    @Upsert suspend fun upsertRecipe(recipe: RecipeEntity)
    @Upsert suspend fun upsertIngredients(items: List<RecipeIngredientEntity>)
    @Upsert suspend fun upsertMealTypes(items: List<RecipeMealTypeEntity>)
    @Upsert suspend fun upsertCategories(items: List<RecipeCategoryEntity>)

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeWithRelations(id: String): RecipeWithRelations?

    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun getIngredients(recipeId: String): List<RecipeIngredientEntity>

    /** Subset of `ids` already present in the cache (to download only missing recipes). */
    @Query("SELECT id FROM recipes WHERE id IN (:ids)")
    suspend fun existingIds(ids: List<String>): List<String>

    @Transaction
    @Query(
        "SELECT r.* FROM recipes r " +
            "INNER JOIN favorites f ON f.recipeId = r.id ORDER BY f.savedDate DESC",
    )
    fun observeFavorites(): Flow<List<RecipeWithRelations>>

    /** Inserts/updates a recipe with all its relations in a single transaction. */
    @Transaction
    suspend fun upsertFull(
        recipe: RecipeEntity,
        ingredients: List<RecipeIngredientEntity>,
        mealTypes: List<RecipeMealTypeEntity>,
        categories: List<RecipeCategoryEntity>,
    ) {
        upsertRecipe(recipe)
        upsertIngredients(ingredients)
        upsertMealTypes(mealTypes)
        upsertCategories(categories)
    }
}
