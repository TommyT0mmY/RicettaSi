package it.unibo.psm.ricettasi.data.remote.datasource

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import it.unibo.psm.ricettasi.data.remote.dto.SyncVersionsDto

/** Calls the sync-specific RPC that returns version counters for each data scope. */
class SyncVersionsRemoteDataSource(
    private val client: SupabaseClient,
) {
    suspend fun getSyncVersions(): SyncVersionsDto =
        client.postgrest.rpc("get_sync_versions").decodeAs()
}