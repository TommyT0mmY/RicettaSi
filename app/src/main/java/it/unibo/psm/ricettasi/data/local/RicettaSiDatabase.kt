package it.unibo.psm.ricettasi.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import it.unibo.psm.ricettasi.data.local.dao.BadgeDao
import it.unibo.psm.ricettasi.data.local.dao.CategoryCacheDao
import it.unibo.psm.ricettasi.data.local.dao.CookedRecipeDao
import it.unibo.psm.ricettasi.data.local.dao.FavoriteDao
import it.unibo.psm.ricettasi.data.local.dao.IngredientDao
import it.unibo.psm.ricettasi.data.local.dao.PantryDao
import it.unibo.psm.ricettasi.data.local.dao.ProfileDao
import it.unibo.psm.ricettasi.data.local.dao.RecipeDao
import it.unibo.psm.ricettasi.data.local.dao.SyncQueueDao
import it.unibo.psm.ricettasi.data.local.entity.BadgeEntity
import it.unibo.psm.ricettasi.data.local.entity.CategoryCacheEntity
import it.unibo.psm.ricettasi.data.local.entity.CookedRecipeEntity
import it.unibo.psm.ricettasi.data.local.entity.FavoriteEntity
import it.unibo.psm.ricettasi.data.local.entity.IngredientCacheEntity
import it.unibo.psm.ricettasi.data.local.entity.IngredientFts
import it.unibo.psm.ricettasi.data.local.entity.PantryItemEntity
import it.unibo.psm.ricettasi.data.local.entity.RecipeCategoryEntity
import it.unibo.psm.ricettasi.data.local.entity.RecipeEntity
import it.unibo.psm.ricettasi.data.local.entity.RecipeIngredientEntity
import it.unibo.psm.ricettasi.data.local.entity.RecipeMealTypeEntity
import it.unibo.psm.ricettasi.data.local.entity.SyncQueueEntity
import it.unibo.psm.ricettasi.data.local.entity.UserBadgeEntity
import it.unibo.psm.ricettasi.data.local.entity.UserProfileEntity

@Database(
    entities = [
        IngredientCacheEntity::class,
        IngredientFts::class,
        RecipeEntity::class,
        RecipeIngredientEntity::class,
        RecipeMealTypeEntity::class,
        RecipeCategoryEntity::class,
        PantryItemEntity::class,
        FavoriteEntity::class,
        CookedRecipeEntity::class,
        UserProfileEntity::class,
        BadgeEntity::class,
        UserBadgeEntity::class,
        SyncQueueEntity::class,
        CategoryCacheEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class RicettaSiDatabase : RoomDatabase() {

    abstract fun ingredientDao(): IngredientDao
    abstract fun pantryDao(): PantryDao
    abstract fun recipeDao(): RecipeDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun cookedRecipeDao(): CookedRecipeDao
    abstract fun profileDao(): ProfileDao
    abstract fun badgeDao(): BadgeDao
    abstract fun categoryCacheDao(): CategoryCacheDao
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {
        private const val DB_NAME = "ricettasi.db"

        @Volatile
        private var instance: RicettaSiDatabase? = null

        /** Singleton instance, provided to Koin by the database module. */
        fun getInstance(context: Context): RicettaSiDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    RicettaSiDatabase::class.java,
                    DB_NAME,
                ).build().also { instance = it }
            }
    }
}
