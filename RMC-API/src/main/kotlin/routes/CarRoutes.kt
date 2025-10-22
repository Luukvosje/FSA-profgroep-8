package com.profgroep8.Controller.Car

import com.profgroep8.exceptions.ConflictException
import com.profgroep8.interfaces.services.ServiceFactory
import com.profgroep8.models.dto.CalculateCarRequestDTO
import com.profgroep8.models.dto.CreateCarDTO
import com.profgroep8.models.dto.FilterCar
import com.profgroep8.models.dto.UpdateCarDTO
import io.ktor.http.HttpStatusCode
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
                val cars = serviceFactory.carService.getAllCars()
                call.respond(cars)
            }

            // POST: Create a car
            post {
                // Receive the DTO from the request body
                val createCarDTO = call.receive<CreateCarDTO>()

                val cars = serviceFactory.carService.filterCars(FilterCar(
                    licensePlate = createCarDTO.licensePlate,
                ))

                if (cars.isNotEmpty())
                    throw ConflictException("Car with license plate ${createCarDTO.licensePlate} already exists")

                // Call the service to create a new car
                // TODO: UserContext instead of ID 1
                val car = serviceFactory.carService.create(createCarDTO, 1)
                    ?: throw BadRequestException("Car not created")

                // Then return it
                call.respond(HttpStatusCode.Created, car)
            }

            route("/{carID}") {
                // GET: Get single car by ID
                get {
                    // Get the id from the params else throw 404
                    val carId = call.parameters["carID"]?.toIntOrNull()
                        ?: throw NotFoundException("Car not found")

                    val car = serviceFactory.carService.getSingle(carId)
                        ?: throw NotFoundException("Car not found")

                    call.respond(car)
                }

                // PUT: Update car by ID
                put {
                    // Get the id from the params else throw 404
                    val carId = call.parameters["carID"]?.toIntOrNull()
                        ?: throw NotFoundException("Car not found")

                    // Get the update request form the body
                    val updateCarDTO = call.receive<UpdateCarDTO>()

                    // Call the update in the service and return it
                    val updatedCar = serviceFactory.carService.update(carId, updateCarDTO)
                        ?: throw BadRequestException("Car not updated")

                    call.respond(updatedCar)
                }

                // DELETE: Delete the car by ID
                delete {
                    // Get the id from the params else throw 404
                    val carId = call.parameters["carID"]?.toIntOrNull()
                        ?: throw NotFoundException("Car not found")

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
                    val carID = call.parameters["carID"]?.toIntOrNull()
                        ?: throw NotFoundException()

                    val car = serviceFactory.carService.getSingle(carID)
                        ?: throw NotFoundException()

                    // Calculate and then return it
                    val res = serviceFactory.carService.calculateCar(request, car)

                    call.respond(res)
                }

                // TODO(make it authenticated to check userid)
                // POST: Upload an image to a car
                post("/image") {
                    val carId = call.parameters["carID"]?.toIntOrNull()
                        ?: throw BadRequestException("Car not found")

                    // Check if car exists
                    serviceFactory.carService.getSingle(carId)
                        ?: throw NotFoundException("Car not found")

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
                val licensePlate = call.parameters["plate"]
                    ?: throw NotFoundException("License plate not found")

                val car = serviceFactory.carService.findByLicense(licensePlate)
                    ?: throw NotFoundException("Car not found")

                call.respond(car)
            }

            // GET: All cars by keyword
            get("/search") {
                val keyword = call.queryParameters["keyword"]

                val cars = serviceFactory.carService.searchCars(keyword)

                call.respond(cars)
            }

            // GET: All cars by filter for each property
            post("filter") {
                val filterObj = call.receive<FilterCar>()

                val cars = serviceFactory.carService.filterCars(filterObj)

                call.respond(cars)
            }

            // TODO(need rentalService for this)
            // GET: All available cars
            get("available") {

            }

            // TODO(maybe make private)
            // GET: All cars by UserID
            get("user/{userID}") {
                val userID = call.parameters["userID"]?.toIntOrNull()
                    ?: throw BadRequestException("User not found")

                val cars = serviceFactory.carService.getCarsByUserId(userID)

                call.respond(cars)
            }
        }
    }
}
