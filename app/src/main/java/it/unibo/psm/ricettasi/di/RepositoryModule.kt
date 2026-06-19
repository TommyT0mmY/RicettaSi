package it.unibo.psm.ricettasi.di

import it.unibo.psm.ricettasi.data.repository.IngredientRepositoryImpl
import it.unibo.psm.ricettasi.data.repository.PantryRepositoryImpl
import it.unibo.psm.ricettasi.data.repository.ProfileRepositoryImpl
import it.unibo.psm.ricettasi.data.repository.SessionRepositoryImpl
import it.unibo.psm.ricettasi.domain.repository.IngredientRepository
import it.unibo.psm.ricettasi.domain.repository.PantryRepository
import it.unibo.psm.ricettasi.domain.repository.ProfileRepository
import it.unibo.psm.ricettasi.domain.repository.SessionRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<SessionRepository> { SessionRepositoryImpl(get(), get(), get(), get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(get(), get(), get(), get(), get()) }
    single<PantryRepository> { PantryRepositoryImpl(get(), get(), get(), get()) }
    single<IngredientRepository> { IngredientRepositoryImpl(get(), get(), get(), get()) }
}