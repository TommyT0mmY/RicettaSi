package it.unibo.psm.ricettasi.data.remote.datasource

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import it.unibo.psm.ricettasi.data.remote.dto.RecipeCategoryDto
import it.unibo.psm.ricettasi.data.remote.dto.RecipeDto
import it.unibo.psm.ricettasi.data.remote.dto.RecipeIngredientDto
import it.unibo.psm.ricettasi.data.remote.dto.RecipeMealTypeDto
import it.unibo.psm.ricettasi.data.remote.dto.RecipeSearchResultDto
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray

/**
 * SupabaseClient wrapper to talk to the recipe-related tables on the server.
 *
 * Every list (esplora, suggerimenti, svuota-frigo) goes through the single
 * [searchRecipes] method, which calls the `search_recipes` RPC. The recipe
 * detail screen uses [getById], which returns the full recipe with steps and relations.
 */
class RecipeRemoteDataSource(
    private val client: SupabaseClient,
) {
    /**
     * Select on `recipes` with the M2M relations embedded. Each relation only picks
     * columns from the junction table itself, never through the FK, so the queries
     * stay consistent and the DTOs flat.
     */
    private val fullColumns = Columns.raw(
        "*," +
            "${RecipeIngredientDto.TABLE}(${RecipeIngredientDto.COL_INGREDIENT_ID},${RecipeIngredientDto.COL_QUANTITY})," +
            "${RecipeMealTypeDto.TABLE}(${RecipeMealTypeDto.COL_MEAL_TYPE})," +
            "${RecipeCategoryDto.TABLE}(${RecipeCategoryDto.COL_CATEGORY_ID})",
    )

    /**
     * Calls the search_recipes RPC. All parameters are optional; omitting one skips that filter.
     * Filters are combined with AND. Inside a list, difficulties and timeWindows use OR;
     * categories and ingredientRoots use AND.
     * categories holds category names (categories.name), not IDs.
     * ingredientRoots must already be root ingredient IDs (the RPC does no synonym resolution).
     * Pantry matching is done server-side; the RPC reads the caller's pantry directly.
     * orderBy: 'match' | 'expiring' | null (title/relevance, default).
     */
    suspend fun searchRecipes(
        query: String? = null,
        difficulties: List<String> = emptyList(),
        timeWindows: List<String> = emptyList(),
        mealType: String? = null,
        categories: List<String> = emptyList(),
        ingredientRoots: List<String> = emptyList(),
        minAvailable: Int = 0,
        minExpiring: Int = 0,
        orderBy: String? = null,
        limit: Int = 60,
    ): List<RecipeSearchResultDto> =
        client.postgrest.rpc(
            function = "search_recipes",
            parameters = buildJsonObject {
                put("p_query", query?.let { JsonPrimitive(it) } ?: JsonNull)
                putJsonArray("p_difficulties") {
                    difficulties.forEach { add(JsonPrimitive(it)) }
                }
                putJsonArray("p_time_windows") {
                    timeWindows.forEach { add(JsonPrimitive(it)) }
                }
                put("p_meal_type", mealType?.let { JsonPrimitive(it) } ?: JsonNull)
                putJsonArray("p_categories") {
                    categories.forEach { add(JsonPrimitive(it)) }
                }
                putJsonArray("p_ingredient_roots") {
                    ingredientRoots.forEach { add(JsonPrimitive(it)) }
                }
                put("p_min_available", JsonPrimitive(minAvailable))
                put("p_min_expiring", JsonPrimitive(minExpiring))
                put("p_order_by", orderBy?.let { JsonPrimitive(it) } ?: JsonNull)
                put("p_limit", JsonPrimitive(limit))
            },
        ).decodeList()

    /**
     * @return The full recipe (with steps and relations) for the detail screen.
     */
    suspend fun getById(id: String): RecipeDto? =
        client.from(RecipeDto.TABLE).select(fullColumns) {
            filter { eq(RecipeDto.COL_ID, id) }
        }.decodeSingleOrNull()
}
