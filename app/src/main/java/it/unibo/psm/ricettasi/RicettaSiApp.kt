package it.unibo.psm.ricettasi

import android.app.Application
import it.unibo.psm.ricettasi.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class RicettaSiApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@RicettaSiApp)
            modules(appModules)
        }
    }
}
