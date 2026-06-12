package it.unibo.psm.ricettasi.di

import it.unibo.psm.ricettasi.ui.screens.auth.AuthViewModel
import it.unibo.psm.ricettasi.ui.screens.pantry.PantryViewModel
import org.koin.dsl.module

val viewModelModule = module {
    factory { AuthViewModel(get()) }
    factory { PantryViewModel(get(), get()) }
}