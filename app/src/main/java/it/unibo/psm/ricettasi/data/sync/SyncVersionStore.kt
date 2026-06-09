package it.unibo.psm.ricettasi.data.sync

import android.content.Context

/** Version counters from the last successful synchronization. */
data class SyncVersions(
    val global: Long = 0,
    val personal: Long = 0,
    val data: Long = 0,
)

/**
 * Persists `global_version` / `personal_version` / `data_version` counters to SharedPreferences
 * for the last successful refresh. Comparison against the values returned by
 * `get_sync_versions()` decides what to re-download.
 */
class SyncVersionStore(context: Context) {

    private val prefs = context.getSharedPreferences("sync_versions", Context.MODE_PRIVATE)

    fun current() = SyncVersions(
        global = prefs.getLong("global_version", 0),
        personal = prefs.getLong("personal_version", 0),
        data = prefs.getLong("data_version", 0),
    )

    fun setGlobal(value: Long) = prefs.edit().putLong("global_version", value).apply()
    fun setPersonal(value: Long) = prefs.edit().putLong("personal_version", value).apply()
    fun setData(value: Long) = prefs.edit().putLong("data_version", value).apply()

    /** Resets counters so the next sync downloads everything from scratch. */
    fun clear() = prefs.edit().clear().apply()
}
