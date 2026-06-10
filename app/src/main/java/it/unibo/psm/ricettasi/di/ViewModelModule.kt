package it.unibo.psm.ricettasi.di

import it.unibo.psm.ricettasi.ui.screens.auth.AuthViewModel
import org.koin.dsl.module

val viewModelModule = module {
    factory { AuthViewModel(get()) }
}