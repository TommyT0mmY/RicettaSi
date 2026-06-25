package it.unibo.psm.ricettasi.di

import it.unibo.psm.ricettasi.data.sync.SyncLock
import it.unibo.psm.ricettasi.data.sync.SyncManager
import it.unibo.psm.ricettasi.data.sync.SyncVersionStore
import it.unibo.psm.ricettasi.data.sync.SyncWriter
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val syncModule = module {
    single { SyncVersionStore(androidContext()) }
    single { SyncLock() }
    single { SyncWriter(db = get(), syncQueue = get(), syncManager = get()) }
    single {
        SyncManager(
            syncVersionRemote = get(),
            ingredientRemote = get(),
            pantryRemote = get(),
            favoriteRemote = get(),
            cookedRecipeRemote = get(),
            userRemote = get(),
            recipeRemote = get(),
            versionStore = get(),
            session = get(),
            syncLock = get(),
            ingredientDao = get(),
            pantryDao = get(),
            favoriteDao = get(),
            cookedRecipeDao = get(),
            badgeDao = get(),
            profileDao = get(),
            recipeDao = get(),
            categoryDao = get(),
            syncQueueDao = get(),
        )
    }
}
