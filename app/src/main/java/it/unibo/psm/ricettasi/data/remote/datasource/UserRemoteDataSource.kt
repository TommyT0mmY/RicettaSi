package it.unibo.psm.ricettasi.data.remote.datasource

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import it.unibo.psm.ricettasi.data.remote.dto.BadgeDto
import it.unibo.psm.ricettasi.data.remote.dto.CategoryDto
import it.unibo.psm.ricettasi.data.remote.dto.UserBadgeDto
import it.unibo.psm.ricettasi.data.remote.dto.UserProfileDto

class UserRemoteDataSource(
    private val client: SupabaseClient,
) {
    suspend fun fetchProfile(userId: String): UserProfileDto? =
        client.from(UserProfileDto.TABLE)
            .select { filter { eq(UserProfileDto.COL_USER_ID, userId) } }
            .decodeSingleOrNull()

    /** Fetches only the columns the client can read (RLS hides the unlock criteria column). */
    suspend fun fetchBadges(): List<BadgeDto> =
        client.from(BadgeDto.TABLE)
            .select(Columns.list(BadgeDto.COL_ID, BadgeDto.COL_CODE, BadgeDto.COL_NAME, BadgeDto.COL_DESCRIPTION))
            .decodeList()

    suspend fun fetchUserBadges(userId: String): List<UserBadgeDto> =
        client.from(UserBadgeDto.TABLE)
            .select { filter { eq(UserBadgeDto.COL_USER_ID, userId) } }
            .decodeList()

    suspend fun fetchCategories(): List<CategoryDto> =
        client.from(CategoryDto.TABLE)
            .select()
            .decodeList()
}
