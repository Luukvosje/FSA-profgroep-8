package com.profgroep8.Controller.Car

import io.ktor.server.application.*
import io.ktor.server.response.respondText
import io.ktor.server.routing.*

fun Application.carRoutes() {
    routing {
        route("/cars") {
            get {
                call.respondText("Hello World!")
            }

//            get("{id}") {
//                carController.getCarById(call)
//            }
//            post {
//                carController.createCar(call)
//            }
//            put("{id}") {
//                carController.updateCar(call)
//            }
//            delete("{id}") {
//                carController.deleteCar(call)
//            }
        }
    }
}
