package com.profgroep8.plugins

import com.profgroep8.Controller.Car.carRoutes
import com.profgroep8.services.ServiceFactoryImpl
import io.ktor.server.application.*

fun Application.configureRouting() {
    val serviceFactory = ServiceFactoryImpl()

    carRoutes(serviceFactory)
}
