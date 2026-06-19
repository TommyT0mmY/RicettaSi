package it.unibo.psm.ricettasi.di

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import it.unibo.psm.ricettasi.BuildConfig
import it.unibo.psm.ricettasi.data.remote.datasource.CookedRecipeRemoteDataSource
import it.unibo.psm.ricettasi.data.remote.datasource.FavoriteRemoteDataSource
import it.unibo.psm.ricettasi.data.remote.datasource.IngredientRemoteDataSource
import it.unibo.psm.ricettasi.data.remote.datasource.PantryRemoteDataSource
import it.unibo.psm.ricettasi.data.remote.datasource.RecipeRemoteDataSource
import it.unibo.psm.ricettasi.data.remote.datasource.UserRemoteDataSource
import it.unibo.psm.ricettasi.data.remote.datasource.SyncVersionsRemoteDataSource
import org.koin.dsl.module

val networkModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
        ) {
            install(Postgrest)
            install(Auth)
        }
    }

    single { RecipeRemoteDataSource(get()) }
    single { IngredientRemoteDataSource(get()) }
    single { PantryRemoteDataSource(get()) }
    single { FavoriteRemoteDataSource(get()) }
    single { CookedRecipeRemoteDataSource(get()) }
    single { UserRemoteDataSource(get()) }
    single { SyncVersionsRemoteDataSource(get()) }
}
