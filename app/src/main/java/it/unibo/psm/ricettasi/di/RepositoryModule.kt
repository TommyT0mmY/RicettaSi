package it.unibo.psm.ricettasi.di

import it.unibo.psm.ricettasi.data.repository.SessionRepositoryImpl
import it.unibo.psm.ricettasi.domain.repository.SessionRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<SessionRepository> { SessionRepositoryImpl(get(), get(), get()) }
}