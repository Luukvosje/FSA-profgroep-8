package com.profgroep8.Controller.Car

import com.profgroep8.exceptions.NotFoundException
import com.profgroep8.repositories.CarRepository
import io.ktor.server.application.*
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.*

fun Application.carRoutes() {
    routing {
        route("/cars") {
            get() {
                val cars = CarRepository().getAll()
                call.respond(cars)
            }
        }
    }
}
