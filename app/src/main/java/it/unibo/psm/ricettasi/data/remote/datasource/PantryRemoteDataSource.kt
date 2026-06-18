package it.unibo.psm.ricettasi.data.remote.datasource

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import it.unibo.psm.ricettasi.data.remote.dto.PantryItemDto

class PantryRemoteDataSource(
    private val client: SupabaseClient,
) {
    suspend fun fetchPantry(userId: String): List<PantryItemDto> =
        client.from(PantryItemDto.TABLE)
            .select { filter { eq(PantryItemDto.COL_USER_ID, userId) } }
            .decodeList()

    suspend fun upsertPantryItem(dto: PantryItemDto) {
        client.from(PantryItemDto.TABLE).upsert(dto)
    }

    suspend fun deletePantryItem(id: String) {
        client.from(PantryItemDto.TABLE).delete { filter { eq(PantryItemDto.COL_ID, id) } }
    }
}
