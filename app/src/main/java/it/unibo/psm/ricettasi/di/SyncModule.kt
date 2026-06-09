package it.unibo.psm.ricettasi.di

import it.unibo.psm.ricettasi.data.sync.SyncVersionStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val syncModule = module {
    single { SyncVersionStore(androidContext()) }
}