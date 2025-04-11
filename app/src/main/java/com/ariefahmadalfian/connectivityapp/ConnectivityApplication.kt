package com.ariefahmadalfian.connectivityapp

import android.app.Application
import com.ariefahmadalfian.connectivityapp.core.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ConnectivityApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@ConnectivityApplication)
            modules(appModule)
        }
    }
}