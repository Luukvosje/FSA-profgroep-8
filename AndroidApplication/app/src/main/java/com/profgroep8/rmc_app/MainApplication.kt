package com.profgroep8.rmc_app


import android.app.Application
import com.example.network.di.networkModule

import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MainApplication)
            modules(networkModule, viewModelModule)
        }
    }
}