package it.unibo.psm.ricettasi.data.remote.datasource

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import it.unibo.psm.ricettasi.data.remote.dto.FavoriteDto

class FavoriteRemoteDataSource(
    private val client: SupabaseClient,
) {
    suspend fun fetchFavorites(userId: String): List<FavoriteDto> =
        client.from(FavoriteDto.TABLE)
            .select { filter { eq(FavoriteDto.COL_USER_ID, userId) } }
            .decodeList()

    suspend fun upsertFavorite(dto: FavoriteDto) {
        client.from(FavoriteDto.TABLE).upsert(dto)
    }

    suspend fun deleteFavorite(userId: String, recipeId: String) {
        client.from(FavoriteDto.TABLE).delete {
            filter {
                eq(FavoriteDto.COL_USER_ID, userId)
                eq(FavoriteDto.COL_RECIPE_ID, recipeId)
            }
        }
    }
}
