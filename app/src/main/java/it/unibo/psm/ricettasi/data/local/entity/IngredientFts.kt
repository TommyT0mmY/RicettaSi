package it.unibo.psm.ricettasi.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4

/**
 * FTS4 full-text search index for [IngredientCacheEntity].
 * Room auto-syncs this table when the content entity changes.
 */
@Fts4(contentEntity = IngredientCacheEntity::class)
@Entity(tableName = "ingredient_cache_fts")
data class IngredientFts(
    val name: String,
)
