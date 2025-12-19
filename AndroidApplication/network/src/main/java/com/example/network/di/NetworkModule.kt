package com.example.network.di

import com.example.network.interfaces.services.ServiceFactory
import com.example.network.services.ServiceFactoryImpl
import io.ktor.client.*
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

import org.koin.dsl.module

val networkModule = module {
    single<ServiceFactory> { ServiceFactoryImpl() }
}