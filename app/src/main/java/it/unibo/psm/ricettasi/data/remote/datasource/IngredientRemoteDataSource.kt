package it.unibo.psm.ricettasi.data.remote.datasource

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import it.unibo.psm.ricettasi.data.remote.dto.IngredientDto

class IngredientRemoteDataSource(
    private val client: SupabaseClient,
) {
    suspend fun fetchGlobalIngredients(): List<IngredientDto> =
        client.from(IngredientDto.TABLE)
            .select { filter { eq(IngredientDto.COL_CREATED_BY_USER, false) } }
            .decodeList()

    suspend fun fetchPersonalIngredients(userId: String): List<IngredientDto> =
        client.from(IngredientDto.TABLE)
            .select { filter { eq(IngredientDto.COL_USER_ID, userId) } }
            .decodeList()

    suspend fun upsertIngredient(dto: IngredientDto) {
        client.from(IngredientDto.TABLE).upsert(dto)
    }

    suspend fun deleteIngredient(id: String) {
        client.from(IngredientDto.TABLE).delete { filter { eq(IngredientDto.COL_ID, id) } }
    }
}
