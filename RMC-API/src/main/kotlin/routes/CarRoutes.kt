package com.profgroep8.Controller.Car

import com.profgroep8.exceptions.ConflictException
import com.profgroep8.exceptions.UnauthorizedException
import com.profgroep8.interfaces.services.ServiceFactory
import com.profgroep8.models.dto.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import requireUserContext
import java.io.File

fun Application.carRoutes(serviceFactory: ServiceFactory) {

    routing {
        authenticate("jwt") {
            route("/cars") {
                // GET: Get all cars
                get() {
                    // Get cars from the service and return it
                    val cars = serviceFactory.carService.getAllCars()
                    call.respond(cars)
                }

                // POST: Create a car
                post {
                    val user = call.requireUserContext()

                    // Receive the DTO from the request body
                    val createCarDTO = call.receive<CreateCarDTO>()

                    val cars = serviceFactory.carService.filterCars(
                        FilterCar(
                            licensePlate = createCarDTO.licensePlate,
                        )
                    )

                    if (cars.isNotEmpty())
                        throw ConflictException("Car with license plate ${createCarDTO.licensePlate} already exists")

                    // Call the service to create a new car
                    val car = serviceFactory.carService.create(createCarDTO, user.userID)
                        ?: throw BadRequestException("Car not created")

                    // Then return it
                    call.respond(HttpStatusCode.Created, car)
                }

                post("/license/{plate}") {
                    val user = call.requireUserContext()
                    val licensePlate = call.parameters["plate"]
                        ?: throw IllegalArgumentException("Missing plate")

                    val cars = serviceFactory.carService.filterCars(
                        FilterCar(
                            licensePlate = licensePlate.toString(),
                        )
                    )

                    if (cars.isNotEmpty())
                        throw ConflictException("Car with license plate ${licensePlate} already exists")

                    val carCreate: CreateCarDTO = serviceFactory.carService.findByLicense(licensePlate)
                        ?: throw BadRequestException("Car could not be found")

                    // Call the service to create a new car
                    val car = serviceFactory.carService.create(carCreate, user.userID)
                        ?: throw BadRequestException("Car not created")

                    // Then return it
                    call.respond(HttpStatusCode.Created, car)
                }

                // GET: get all available Cars
                get("/available/{date}") {
                    val dateParam = call.parameters["date"]

                    val date = LocalDateTime.parse(dateParam?: java.time.LocalDateTime.now().toString())

                    val cars = serviceFactory.carService.getAvailableCars(date)
                    call.respond(cars)
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
                            ?: throw NotFoundException("Car not updated")

                        call.respond(updatedCar)
                    }

                    // DELETE: Delete the car by ID
                    delete {
                        // Get the id from the params else throw 404
                        val carId = call.parameters["carID"]?.toIntOrNull()
                            ?: throw NotFoundException("Car not found")

                        // Delete it in the service
                        val success = serviceFactory.carService.delete(carId)

                        // If failed throw 404
                        if (!success) throw NotFoundException("Car could not be deleted")

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

                    // POST: Upload an image to a car
                    post("/image") {

                        val carId = call.parameters["carID"]?.toIntOrNull()
                            ?: throw BadRequestException("Car not found")

                        // Check if car exists
                        val car = serviceFactory.carService.getSingle(carId)
                            ?: throw NotFoundException("Car not found")

                        if (car.userID != call.requireUserContext().userID) {
                            throw UnauthorizedException()
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
}
