package com.profgroep8.Controller.Car

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.carRoutes(carController: CarController) {
    routing {
        route("/cars") {
            get {
                carController.getAllCars(call)
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
