package it.unibo.psm.ricettasi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local copy of a row from the `public.categories` table, kept so the app
 * can resolve a `categoryId` to its display name without going back to
 * the server. The table is tiny (around 50 rows) and refreshed together
 * with the other global lookup tables during sync.
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
)
