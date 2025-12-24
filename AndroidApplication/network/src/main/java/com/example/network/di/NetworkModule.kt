package com.example.network.di

import com.example.network.interfaces.services.ServiceFactory
import com.example.network.services.ServiceFactoryImpl
import com.example.network.services.TokenProvider
import org.koin.dsl.module

val networkModule = module {
    single { TokenProvider }
    single<ServiceFactory> { ServiceFactoryImpl() }
}