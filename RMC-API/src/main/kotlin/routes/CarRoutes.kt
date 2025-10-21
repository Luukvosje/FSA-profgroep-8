package com.profgroep8.Controller.Car

import com.profgroep8.interfaces.services.ServiceFactory
import com.profgroep8.models.dto.CalculateCarRequestDTO
import com.profgroep8.models.dto.CreateCarDTO
import com.profgroep8.models.dto.FilterCar
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
            // GET: Get all cars
            get() {
                // Get cars from the service and return it
                val cars = serviceFactory.carService.getAll()
                call.respond(cars)
            }

            // POST: Create a car
            post {
                // Receive the DTO from the request body
                val createCarDTO = call.receive<CreateCarDTO>()

                // Call the service to create a new car
                val newCar = serviceFactory.carService.create(createCarDTO)

                // Then return it
                call.respond(newCar)
            }

            route("/{id}") {
                // GET: Get single car by ID
                get {
                    // Get the id from the params else throw 404
                    val carId = call.parameters["id"]?.toIntOrNull() ?: throw NotFoundException()

                    val car = serviceFactory.carService.getSingle(carId)
                    call.respond(car)
                }

                // PUT: Update car by ID
                put {
                    // Get the id from the params else throw 404
                    val carId = call.parameters["id"]?.toIntOrNull() ?: throw NotFoundException()

                    // Get the update request form the body
                    val updateCarDTO = call.receive<UpdateCarDTO>()

                    // Call the update in the service and return it
                    val updatedCar = serviceFactory.carService.update(carId, updateCarDTO)
                    call.respond(updatedCar)
                }

                // DELETE: Delete the car by ID
                delete("/{id}") {
                    // Get the id from the params else throw 404
                    val carId = call.parameters["id"]?.toIntOrNull() ?: throw NotFoundException()

                    // Delete it in the service
                    val success = serviceFactory.carService.delete(carId)

                    // If failed throw 401
                    if (!success) throw BadRequestException("Car could not be deleted")

                    // Return true
                    call.respond(true)
                }

                // POST: Calculate the total cost of a car
                post("/calculate") {
                    // Get the request from the body
                    val request = call.receive<CalculateCarRequestDTO>();

                    // Validate standardKmPerYear
                    if (request.standardKmPerYear.isNaN()) {
                        throw NotFoundException("Request is missing, ${request}")
                    }

                    // Get the id from the params else throw 404
                    request.carId = call.parameters["id"]?.toIntOrNull() ?: throw NotFoundException()

                    // Calculate and then return it
                    val res = serviceFactory.carService.calculateCar(request)
                        ?: throw BadRequestException("Car could not be calculated")

                    call.respond(res)
                }

                // TODO(make it authenticated to check userid)
                // POST: Upload an image to a car
                post("/image") {
                    val carId = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid car ID")

                    val checkCarId = serviceFactory.carService.getSingle(carId)
                    if (checkCarId === null) {
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

            // GET: Get car from RdwClient by license plate
            get("/license/{plate}") {
                val licensePlate = call.parameters["plate"] ?: throw NotFoundException()

                val car = serviceFactory.carService.findByLicense(licensePlate)
                if (car === null) throw BadRequestException("Car could not be found")

                call.respond(car)
            }

            // GET: All cars by keyword
            get("/search") {
                val keyword = call.queryParameters["keyword"]

                val cars = serviceFactory.carService.searchCars(keyword ?: "")

                call.respond(cars)
            }

            // GET: All cars by filter for each property
            post("filter") {
                val filterObj = call.receive<FilterCar>()

                val cars = serviceFactory.carService.filterCar(filterObj) ?: throw BadRequestException("Cars could not be filtered")
                call.respond(cars)
            }

            // TODO(need rentalService for this)
            // GET: All available cars
            get("available") {

            }

            // TODO(maybe make private)
            // GET: All cars by UserID
            get("user/{userId}") {
                val userId = call.parameters["userId"]?.toIntOrNull() ?: throw BadRequestException("Invalid user ID")
                val cars = serviceFactory.carService.getCarsByUserId(userId)

                if (cars === null) throw BadRequestException("User has no cars")

                call.respond(cars)
            }
        }
    }
}
