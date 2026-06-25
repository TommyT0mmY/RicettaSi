package it.unibo.psm.ricettasi.data.remote.datasource

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import it.unibo.psm.ricettasi.data.remote.dto.CookedRecipeDto

class CookedRecipeRemoteDataSource(
    private val client: SupabaseClient,
) {
    suspend fun fetchCookedRecipes(userId: String): List<CookedRecipeDto> =
        client.from(CookedRecipeDto.TABLE)
            .select { filter { eq(CookedRecipeDto.COL_USER_ID, userId) } }
            .decodeList()

    /** The upsert triggers XP gain and server-side badge re-evaluation. */
    suspend fun upsertCookedRecipe(dto: CookedRecipeDto) {
        client.from(CookedRecipeDto.TABLE).upsert(dto)
    }

    suspend fun deleteCookedRecipe(id: String) {
        client.from(CookedRecipeDto.TABLE).delete { filter { eq(CookedRecipeDto.COL_ID, id) } }
    }
}
