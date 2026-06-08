package it.unibo.psm.ricettasi.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Pantry item. No merging: two rows with the same [ingredientId] are valid and independent. */
@Entity(
    tableName = "pantry_items",
    indices = [Index("ingredientId"), Index("consumed")],
)
data class PantryItemEntity(
    @PrimaryKey val id: String,
    val ingredientId: String,
    val quantity: String?,
    /** ISO `yyyy-MM-dd`. */
    val expiryDate: String?,
    /** Epoch millis. */
    val addedDate: Long,
    val consumed: Boolean,
    /** Epoch millis. */
    val consumedDate: Long?,
)
