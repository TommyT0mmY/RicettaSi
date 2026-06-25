package it.unibo.psm.ricettasi.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local copy of an ingredient row (global ones + this user's personal ingredients).
 * This table backs trigram search via the FTS index and synonym resolution.
 */
@Entity(
    tableName = "ingredients",
    indices = [Index("parentIngredientId")],
)
data class IngredientEntity(
    @PrimaryKey val id: String,
    val name: String,
    val parentIngredientId: String?,
    /** `true` = global admin ingredient, `false` = personal ingredient of this user. */
    val isGlobal: Boolean,
)
