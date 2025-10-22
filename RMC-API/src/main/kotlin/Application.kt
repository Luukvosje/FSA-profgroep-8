package com.profgroep8

import com.profgroep8.exceptions.configureStatusPages
import com.profgroep8.repositories.DatabaseFactoryImpl
import com.profgroep8.plugins.JwtConfig
import com.profgroep8.plugins.configureRouting
import com.profgroep8.plugins.configureSerialization
import com.profgroep8.plugins.configureOpenAPI
import com.profgroep8.services.ServiceFactoryImpl
import configureUserContext
import configureUserContextPlugin
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val appConfig = environment.config
    DatabaseFactoryImpl.init()

    val serviceFactory = ServiceFactoryImpl(DatabaseFactoryImpl, appConfig)

    JwtConfig.init(appConfig)
    JwtConfig.configureSecurity(this)

    //For intergrating the userContext
    configureUserContext()
    configureUserContextPlugin()

    configureSerialization()
    configureRouting(serviceFactory)
    configureStatusPages()
    configureOpenAPI()
}
