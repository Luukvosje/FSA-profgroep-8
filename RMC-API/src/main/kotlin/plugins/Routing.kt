package com.profgroep8.plugins

import com.profgroep8.Controller.Car.carRoutes
import io.ktor.server.application.*

fun Application.configureRouting() {
    carRoutes()
}
