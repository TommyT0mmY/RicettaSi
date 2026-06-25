package it.unibo.psm.ricettasi.di

import it.unibo.psm.ricettasi.data.settings.ThemeStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val settingsModule = module {
    single { ThemeStore(androidContext()) }
}