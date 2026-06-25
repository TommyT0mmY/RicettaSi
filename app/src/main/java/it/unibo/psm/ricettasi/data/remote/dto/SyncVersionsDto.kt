package it.unibo.psm.ricettasi.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The current sync version counters, used to tell whether the locally cached data is
 * out of date: a global counter for the global ingredients, plus the optional personal (ingredients) and data ones.
 */
@Serializable
data class SyncVersionsDto(
    @SerialName(COL_GLOBAL_VERSION) val globalVersion: Long,
    @SerialName(COL_PERSONAL_VERSION) val personalVersion: Long? = null,
    @SerialName(COL_DATA_VERSION) val dataVersion: Long? = null,
) {
    companion object {
        const val COL_GLOBAL_VERSION = "global_version"
        const val COL_PERSONAL_VERSION = "personal_version"
        const val COL_DATA_VERSION = "data_version"
    }
}
