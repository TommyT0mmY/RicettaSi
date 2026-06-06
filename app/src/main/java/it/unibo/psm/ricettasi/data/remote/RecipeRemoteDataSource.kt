package it.unibo.psm.ricettasi.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import it.unibo.psm.ricettasi.data.remote.dto.RecipeDto
import it.unibo.psm.ricettasi.data.remote.dto.RecipeSearchResultDto
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 * Supabase access for catalogue recipes.
 *
 * Tutte le liste (esplora, suggerimenti, svuota-frigo) passano dall'unico metodo
 * [searchRecipes] che chiama la RPC `search_recipes`. Il dettaglio ricetta usa
 * [getById] che restituisce la ricetta completa con steps e relazioni.
 */
class RecipeRemoteDataSource(
    private val client: SupabaseClient,
) {

    /** Select su `recipes` con tutte le relazioni embedded. */
    private val fullColumns = Columns.raw(
        "*," +
            "recipe_ingredients(recipe_id,ingredient_id,quantity,optional)," +
            "recipe_meal_types(recipe_id,meal_type)," +
            "recipe_categories(recipe_id,category)",
    )

    /**
     * Unico metodo di ricerca: chiama la RPC `search_recipes` che filtra
     * server-side per titolo, difficoltà, tempo max, tipo pasto, categoria,
     * ingredienti richiesti e calcola disponibilità contro la dispensa.
     */
    suspend fun searchRecipes(
        query: String? = null,
        difficulty: String? = null,
        maxMinutes: Int? = null,
        mealType: String? = null,
        category: String? = null,
        ingredientRoots: List<String> = emptyList(),
        pantryRoots: List<String> = emptyList(),
        expiringRoots: List<String> = emptyList(),
        minAvailable: Int = 0,
        minExpiring: Int = 0,
        limit: Int = 60,
    ): List<RecipeSearchResultDto> =
        client.postgrest.rpc(
            function = "search_recipes",
            parameters = buildJsonObject {
                put("p_query", query?.let { JsonPrimitive(it) } ?: JsonNull)
                put("p_difficulty", difficulty?.let { JsonPrimitive(it) } ?: JsonNull)
                put("p_max_minutes", maxMinutes?.let { JsonPrimitive(it) } ?: JsonNull)
                put("p_meal_type", mealType?.let { JsonPrimitive(it) } ?: JsonNull)
                put("p_category", category?.let { JsonPrimitive(it) } ?: JsonNull)
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
                put("p_limit", JsonPrimitive(limit))            },
        ).decodeList()

    /** Ricetta completa (con steps e relazioni), per la schermata dettaglio. */
    suspend fun getById(id: String): RecipeDto? =
        client.from("recipes").select(fullColumns) {
            filter { eq("id", id) }
        }.decodeSingleOrNull()
}
