package com.profgroep8.Controller.Car

import com.profgroep8.interfaces.services.ServiceFactory
import com.profgroep8.models.dto.CreateCarDTO
import com.profgroep8.models.dto.UpdateCarDTO
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.*

fun Application.carRoutes(serviceFactory: ServiceFactory) {

    routing {
        route("/cars") {
            get() {
                val cars = serviceFactory.carService.getAll()
                call.respond(cars)
            }

            get("/{id}") {
                val carId = call.parameters["id"]?.toIntOrNull() ?: throw NotFoundException()
                val car = serviceFactory.carService.getSingle(carId)
                call.respond(car)
            }

            post {
                // Receive the DTO from the request body
                val createCarDTO = call.receive<CreateCarDTO>()

                // Call the service to create a new car
                val newCar = serviceFactory.carService.create(createCarDTO)

                // Respond with the created car
                call.respond(newCar)
            }

            put("/{id}") {
                val carId = call.parameters["id"]?.toIntOrNull() ?: throw NotFoundException()
                val updateCarDTO = call.receive<UpdateCarDTO>()

                // Call the service to update the car
                val updatedCar = serviceFactory.carService.update(carId, updateCarDTO)

                call.respond(updatedCar)
            }

            delete("/{id}") {
                val carId = call.parameters["id"]?.toIntOrNull() ?: throw NotFoundException()

                // Call the service to update the car
                val success = serviceFactory.carService.delete(carId)

                if (!success) throw BadRequestException("Car could not be deleted")

                call.respond(true)
            }
        }
    }
}
