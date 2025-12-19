package com.example.fauxtoes

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.annotation.KoinApplication
import org.koin.ksp.generated.startKoin
import timber.log.Timber

@KoinApplication()
class FauxToesApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ProductionTree())
        }

        startKoin {
            androidLogger()
            androidContext(
                androidContext = this@FauxToesApplication
            )
        }
    }
}


class ProductionTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority >= android.util.Log.WARN) {
            android.util.Log.println(priority, tag, message)
        }
    }
}
