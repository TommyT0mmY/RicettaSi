package it.unibo.psm.ricettasi.data.remote.datasource

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import it.unibo.psm.ricettasi.data.remote.dto.IngredientDto

class IngredientRemoteDataSource(
    private val client: SupabaseClient,
) {
    suspend fun fetchGlobalIngredients(): List<IngredientDto> {
        // PostgREST caps a select at 1000 rows, and there are more global ingredients than that,
        // so page through them until a short page comes back. Without this the recipes that use
        // an ingredient past the first 1000 show its raw id instead of the name.
        val pageSize = 1000L
        val all = mutableListOf<IngredientDto>()
        var offset = 0L
        while (true) {
            val page = client.from(IngredientDto.TABLE)
                .select {
                    filter { eq(IngredientDto.COL_CREATED_BY_USER, false) }
                    range(offset, offset + pageSize - 1)
                }
                .decodeList<IngredientDto>()
            all.addAll(page)
            if (page.size < pageSize) break
            offset += pageSize
        }
        return all
    }

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
