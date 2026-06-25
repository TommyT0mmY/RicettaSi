package it.unibo.psm.ricettasi.domain.repository

import it.unibo.psm.ricettasi.domain.model.PantryItem
import kotlinx.coroutines.flow.Flow

/** The user's pantry: active items and the ones moved to "Finished items". */
interface PantryRepository {

    /** Reactive: cross-device sync can update pantry while screen is open. */
    fun observeActive(): Flow<List<PantryItem>>

    /** Reactive: cross-device sync can update "Finished items" while screen is open. */
    fun observeConsumed(): Flow<List<PantryItem>>

    /**
     * Inserts [item], or updates it in place if [PantryItem.id] already exists. Items for the same
     * ingredient are kept as distinct rows and are never merged (see [PantryItem]).
     */
    suspend fun add(item: PantryItem)

    /** Moves between the active pantry and "Finished items". No-op if [id] does not exist. */
    suspend fun setConsumed(id: String, consumed: Boolean)

    /** Permanent deletion (hard delete). No-op if [id] does not exist. */
    suspend fun delete(id: String)
}
