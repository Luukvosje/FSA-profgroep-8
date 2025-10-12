package com.profgroep8.Controller.Car

import com.profgroep8.interfaces.services.ServiceFactory
import com.profgroep8.models.dto.CreateCarDTO
import com.profgroep8.models.dto.UpdateCarDTO
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.jvm.javaio.*
import java.io.File

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

            //check for getting licenseplate
            get("/license/{plate}") {
                val licensePlate = call.parameters["plate"] ?: throw NotFoundException()

                val car = serviceFactory.carService.findByLicense(licensePlate)
                if (car === null) throw BadRequestException("Car could not be found")

                call.respond(car)
            }

            get("/search") {

            }

            route("{id}") {
                //Calculate
                post("/calculate") {}

                //Uploading image
                //Todo make it authenticated to check userid
                post("/image") {
                    val carId = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid car ID")

                    val checkCarId = serviceFactory.carService.getSingle(carId);
                    if (checkCarId == null) {
                        throw NotFoundException("Car could not be found")
                    }

                    val multipart = call.receiveMultipart()
                    var fileName: String? = null

                    multipart.forEachPart { part ->
                        if (part is PartData.FileItem) {
                            val ext = File(part.originalFileName ?: "image.jpg").extension
                            fileName = "car_${carId}.${ext}"

                            val uploadDir = File("uploads/cars")
                            if (!uploadDir.exists()) uploadDir.mkdirs()

                            val file = File(uploadDir, fileName!!)
                            part.provider().copyTo(file.outputStream())
                            part.dispose()
                        }
                    }

                    if (fileName == null) throw BadRequestException("No image uploaded")

                    val filePath = "/uploads/cars/$fileName"

                    call.respond(mapOf("imagePath" to filePath))
                }
            }

            get("filter") {

            }

            get("available") {

            }

            //maybe make private
            get("user/{userId}") {
                val userId = call.parameters["userId"]?.toIntOrNull() ?: throw BadRequestException("Invalid user ID")
                val cars = serviceFactory.carService.getCarsByUserId(userId)

                if (cars === null) throw BadRequestException("User has no cars")

                call.respond(cars)
            }
        }
    }
}
