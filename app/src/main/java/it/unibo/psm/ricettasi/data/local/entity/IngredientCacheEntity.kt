package it.unibo.psm.ricettasi.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local cache of ingredients (the global ones + this user's personal ingredients).
 * This table backs trigram search and synonym resolution.
 */
@Entity(
    tableName = "ingredient_cache",
    indices = [Index("parentIngredientId")],
)
data class IngredientCacheEntity(
    @PrimaryKey val id: String,
    val name: String,
    val parentIngredientId: String?,
    /** `true` = global admin ingredient, `false` = personal ingredient of this user. */
    val isGlobal: Boolean,
)
