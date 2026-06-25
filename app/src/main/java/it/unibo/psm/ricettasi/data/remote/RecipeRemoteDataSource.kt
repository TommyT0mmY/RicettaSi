package it.unibo.psm.ricettasi.data.remote

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
     * The only search method: calls the `search_recipes` RPC, which does all the
     * filtering, pantry matching and ordering server-side. Every parameter is optional:
     * leaving it null (or an empty list) just skips that filter. The filters are combined
     * with AND between each other, but inside a single list parameter it depends on which
     * one it is:
     *
     * - [query]: case insensitive substring match on the title, or a typo-tolerant match.
     * - [difficulties]: OR match, the recipe must have one of these difficulty values.
     * - [timeWindows]: OR match on the preparation time, bucketed into 'quick' (<= 15 min),
     *   'medium' (16 to 30 min) or 'long' (more than 30 min).
     * - [mealType]: single value, the recipe must be linked to this meal type.
     * - [categories]: AND match, the recipe must have ALL the given category names, not
     *   just one of them.
     * - [ingredientRoots]: AND match, the recipe must contain ALL of these ingredient ids.
     *   They must already be root ingredient ids, there is no synonym resolution inside
     *   the RPC.
     * - [pantryRoots]: not a filter on its own. It is compared against the recipe's own
     *   ingredients to work out how many of them are in the pantry, the availableCount/
     *   requiredCount pair on [RecipeSearchResultDto] (the pantry match percentage).
     * - [expiringRoots]: same idea but for ingredients close to expiry, used to fill
     *   expiringMatchCount.
     * - [minAvailable]/[minExpiring]: drop recipes whose match counts (computed from
     *   pantryRoots/expiringRoots above) fall below these thresholds.
     * - [orderBy]: 'title' (alphabetical, the default), 'match' (highest pantry match
     *   percentage first) or 'expiring' (most expiring ingredients used first). Recipes
     *   tied on the chosen order, or any other value, fall back to title order.
     * - [limit]: max number of rows returned.
     */
    suspend fun searchRecipes(
        query: String? = null,
        difficulties: List<String> = emptyList(),
        timeWindows: List<String> = emptyList(),
        mealType: String? = null,
        categories: List<String> = emptyList(),
        ingredientRoots: List<String> = emptyList(),
        pantryRoots: List<String> = emptyList(),
        expiringRoots: List<String> = emptyList(),
        minAvailable: Int = 0,
        minExpiring: Int = 0,
        orderBy: String = "title",
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
                putJsonArray("p_pantry_roots") {
                    pantryRoots.forEach { add(JsonPrimitive(it)) }
                }
                putJsonArray("p_expiring_roots") {
                    expiringRoots.forEach { add(JsonPrimitive(it)) }
                }
                put("p_min_available", JsonPrimitive(minAvailable))
                put("p_min_expiring", JsonPrimitive(minExpiring))
                put("p_order_by", JsonPrimitive(orderBy))
                put("p_limit", JsonPrimitive(limit))
            },
        ).decodeList()

    /**
     * @return The full recipe (with steps and relations) for the detail screen.
     */
    suspend fun getById(id: String): RecipeDto? =
        client.from("recipes").select(fullColumns) {
            filter { eq(RecipeDto.COL_ID, id) }
        }.decodeSingleOrNull()
}
