package it.unibo.psm.ricettasi.di

import org.koin.core.module.Module

/** All Koin modules for the app, loaded by [it.unibo.psm.ricettasi.RicettaSiApp]. */
val appModules: List<Module> = listOf(
    networkModule,
    databaseModule,
    syncModule,
    // repositoryModule,
    // viewModelModule,
)
