package it.unibo.psm.ricettasi.di

import it.unibo.psm.ricettasi.ui.screens.auth.AuthViewModel
import it.unibo.psm.ricettasi.ui.screens.esplora.EsploraFilterBus
import it.unibo.psm.ricettasi.ui.screens.esplora.EsploraViewModel
import it.unibo.psm.ricettasi.ui.screens.home.HomeViewModel
import it.unibo.psm.ricettasi.ui.screens.pantry.PantryViewModel
import it.unibo.psm.ricettasi.ui.screens.preferiti.PreferitiViewModel
import it.unibo.psm.ricettasi.ui.screens.profile.ProfiloViewModel
import it.unibo.psm.ricettasi.ui.screens.recipe.RecipeDetailViewModel
import it.unibo.psm.ricettasi.ui.screens.settings.SettingsViewModel
import it.unibo.psm.ricettasi.ui.screens.svuotailfrigo.SvuotaIlFrigoViewModel
import org.koin.dsl.module

val viewModelModule = module {
    single { EsploraFilterBus() }

    factory { AuthViewModel(get()) }
    factory { PantryViewModel(get(), get()) }
    factory { HomeViewModel(get(), get(), get()) }
    factory { EsploraViewModel(get(), get(), get(), get()) }
    factory { PreferitiViewModel(get()) }
    factory { ProfiloViewModel(get()) }
    factory { RecipeDetailViewModel(get(), get()) }
    factory { SettingsViewModel(get(), get()) }
    factory { SvuotaIlFrigoViewModel(get(), get(), get()) }
}