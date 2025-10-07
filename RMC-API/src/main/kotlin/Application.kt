package com.profgroep8

import com.profgroep8.exceptions.configureStatusPages
import com.profgroep8.repositories.DatabaseFactoryImpl
import com.profgroep8.plugins.JwtConfig
import com.profgroep8.plugins.configureRouting
import com.profgroep8.plugins.configureSerialization
import com.profgroep8.services.ServiceFactoryImpl
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val appConfig = environment.config
    DatabaseFactoryImpl.init()

    val serviceFactory = ServiceFactoryImpl(DatabaseFactoryImpl)

    JwtConfig.init(appConfig)
    JwtConfig.configureSecurity(this)

    configureSerialization()
    configureRouting(serviceFactory)
    configureStatusPages()
}
