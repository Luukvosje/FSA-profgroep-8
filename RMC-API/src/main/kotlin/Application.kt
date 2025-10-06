package com.profgroep8

import com.profgroep8.Config.DatabaseConfig
import com.profgroep8.Config.JwtConfig
import com.profgroep8.Config.configureRouting
import com.profgroep8.Config.configureSerialization
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val appConfig = environment.config
    DatabaseConfig.init()

    JwtConfig.init(appConfig)
    JwtConfig.configureSecurity(this)

    configureSerialization()
    configureRouting()
}
