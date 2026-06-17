package it.unibo.psm.ricettasi.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.unibo.psm.ricettasi.data.local.entity.CookedRecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CookedRecipeDao {

    // Reactive: cooking a recipe updates the Profile screen counter (UI only reads Flow)
    @Query("SELECT COUNT(DISTINCT recipeId) FROM cooked_recipes")
    fun observeUniqueCount(): Flow<Int>

    @Upsert
    suspend fun upsert(item: CookedRecipeEntity)

    @Upsert
    suspend fun upsertAll(items: List<CookedRecipeEntity>)

    @Query("DELETE FROM cooked_recipes")
    suspend fun clear()
}
