package it.unibo.psm.ricettasi.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import it.unibo.psm.ricettasi.data.local.dao.BadgeDao
import it.unibo.psm.ricettasi.data.local.dao.CategoryDao
import it.unibo.psm.ricettasi.data.local.dao.CookedRecipeDao
import it.unibo.psm.ricettasi.data.local.dao.FavoriteDao
import it.unibo.psm.ricettasi.data.local.dao.IngredientDao
import it.unibo.psm.ricettasi.data.local.dao.PantryDao
import it.unibo.psm.ricettasi.data.local.dao.ProfileDao
import it.unibo.psm.ricettasi.data.local.dao.RecipeDao
import it.unibo.psm.ricettasi.data.local.dao.SyncQueueDao
import it.unibo.psm.ricettasi.data.local.entity.BadgeEntity
import it.unibo.psm.ricettasi.data.local.entity.CategoryEntity
import it.unibo.psm.ricettasi.data.local.entity.CookedRecipeEntity
import it.unibo.psm.ricettasi.data.local.entity.FavoriteEntity
import it.unibo.psm.ricettasi.data.local.entity.IngredientEntity
import it.unibo.psm.ricettasi.data.local.entity.IngredientFtsEntity
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
        IngredientEntity::class,
        IngredientFtsEntity::class,
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
        CategoryEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class RicettaSiDatabase : RoomDatabase() {

    abstract fun ingredientDao(): IngredientDao
    abstract fun pantryDao(): PantryDao
    abstract fun recipeDao(): RecipeDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun cookedRecipeDao(): CookedRecipeDao
    abstract fun profileDao(): ProfileDao
    abstract fun badgeDao(): BadgeDao
    abstract fun categoryDao(): CategoryDao
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
