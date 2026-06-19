package it.unibo.psm.ricettasi.di

import it.unibo.psm.ricettasi.data.local.RicettaSiDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { RicettaSiDatabase.getInstance(androidContext()) }

    single { get<RicettaSiDatabase>().ingredientDao() }
    single { get<RicettaSiDatabase>().pantryDao() }
    single { get<RicettaSiDatabase>().recipeDao() }
    single { get<RicettaSiDatabase>().favoriteDao() }
    single { get<RicettaSiDatabase>().profileDao() }
    single { get<RicettaSiDatabase>().cookedRecipeDao() }
    single { get<RicettaSiDatabase>().syncQueueDao() }
    single { get<RicettaSiDatabase>().badgeDao() }
    single { get<RicettaSiDatabase>().categoryDao() }
}
