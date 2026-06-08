package it.unibo.psm.ricettasi.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cooked_recipes",
    indices = [Index("recipeId")],
)
data class CookedRecipeEntity(
    @PrimaryKey val id: String,
    val recipeId: String,
    /** Epoch millis. */
    val cookedDate: Long,
)
