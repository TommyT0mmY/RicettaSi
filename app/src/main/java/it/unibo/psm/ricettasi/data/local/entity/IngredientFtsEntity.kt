package it.unibo.psm.ricettasi.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4

/**
 * FTS4 full-text search index for [IngredientEntity].
 * Room auto-syncs this table when the content entity changes.
 */
@Fts4(contentEntity = IngredientEntity::class)
@Entity(tableName = "ingredient_fts")
data class IngredientFtsEntity(
    val name: String,
)
