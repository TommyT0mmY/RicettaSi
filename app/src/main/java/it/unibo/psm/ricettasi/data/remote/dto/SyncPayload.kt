package it.unibo.psm.ricettasi.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * The DTOs that can be queued for offline sync. Grouping them under one sealed type lets the
 * queue keep any of them in a single column and decode each row back to the right DTO
 * (kotlinx writes a "type" field into the JSON so it knows which one it was).
 */
@Serializable
sealed interface SyncPayload
