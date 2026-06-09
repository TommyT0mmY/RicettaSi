package it.unibo.psm.ricettasi.di

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import it.unibo.psm.ricettasi.BuildConfig
import it.unibo.psm.ricettasi.data.remote.RecipeRemoteDataSource
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
}
