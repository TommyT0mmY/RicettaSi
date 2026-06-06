package it.unibo.psm.ricettasi.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response from the `get_sync_versions()` RPC. */
@Serializable
data class SyncVersionsDto(
    @SerialName("global_version") val globalVersion: Long,
    @SerialName("personal_version") val personalVersion: Long? = null,
    @SerialName("data_version") val dataVersion: Long? = null,
)
