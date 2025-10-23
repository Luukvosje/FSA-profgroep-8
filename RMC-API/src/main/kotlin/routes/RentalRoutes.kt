package com.profgroep8.Controller.Rental

import com.profgroep8.interfaces.services.ServiceFactory
import com.profgroep8.models.dto.CreateRentalDTO
import com.profgroep8.models.dto.UpdateRentalDTO
import com.profgroep8.models.dto.UpdateRentalLocationDTO
import com.profgroep8.models.dto.RentalLocationsResponseDTO
import getUserContext
import requireUserContext
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import io.ktor.server.auth.*

// Helper function to check if user is either the renter or car owner
fun ApplicationCall.canAccessRental(serviceFactory: ServiceFactory, rentalId: Int): Boolean {
    val userContext = this.getUserContext() ?: return false
    val rental = serviceFactory.rentalService.getSingle(rentalId)
    val car = serviceFactory.carService.getSingle(rental.carID)
    
    // User can access if they are either the renter or the car owner
    return userContext.userID == rental.userID || userContext.userID == car?.userID
}

fun Application.rentalRoutes(serviceFactory: ServiceFactory) {

    routing {
        route("/rentals") {
            // Get all rentals
            authenticate("jwt") {
                get {
                    val userContext = call.requireUserContext()
                    val allRentals = serviceFactory.rentalService.getAll()
                    
                    // Filter rentals to only include those where user is either renter or car owner
                    val userRentals = allRentals.filter { rental ->
                        val car = serviceFactory.carService.getSingle(rental.carID)
                        userContext.userID == rental.userID || userContext.userID == car?.userID
                    }
                    
                    call.respond(userRentals)
                }
            }

            // Get single rental
            authenticate("jwt") {
                get("/{id}") {
                    val rentalId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid rental ID format")
                    
                    // Check if user can access this rental (renter or car owner)
                    if (!call.canAccessRental(serviceFactory, rentalId)) {
                        throw BadRequestException("You can only access rentals you are involved in")
                    }
                    
                    val rental = serviceFactory.rentalService.getSingle(rentalId)
                    call.respond(rental)
                }
            }

            // Get rental locations
            authenticate("jwt") {
                get("/{id}/locations") {
                    val rentalId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid rental ID format")
                    
                    // Check if user can access this rental (renter or car owner)
                    if (!call.canAccessRental(serviceFactory, rentalId)) {
                        throw BadRequestException("You can only access rental locations for rentals you are involved in")
                    }
                    
                    val rental = serviceFactory.rentalService.getSingle(rentalId)
                    
                    val locationsResponse = RentalLocationsResponseDTO(
                        startLocation = rental.startRentalLocation,
                        endLocation = rental.endRentalLocation
                    )
                    
                    call.respond(locationsResponse)
                }
            }

            authenticate("jwt") {
                // Create new rental
                post {
                    val userContext = call.requireUserContext()
                    val createRentalDTO = call.receive<CreateRentalDTO>()
                    val newRental = serviceFactory.rentalService.create(createRentalDTO, userContext.userID)
                    call.respond(newRental)
                }

                // Update rental by id
                put("/{id}") {
                    val rentalId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid rental ID format")
                    
                    // Check if user can access this rental (renter or car owner)
                    if (!call.canAccessRental(serviceFactory, rentalId)) {
                        throw BadRequestException("You can only update rentals you are involved in")
                    }
                    
                    val updateRentalDTO = call.receive<UpdateRentalDTO>()
                    val updatedRental = serviceFactory.rentalService.update(rentalId, updateRentalDTO)
                    call.respond(updatedRental)
                }

                // Set rental to state inactive by id
                put("/{id}/end") {
                    val rentalId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid rental ID format")
                    
                    // Check if user can access this rental (renter or car owner)
                    if (!call.canAccessRental(serviceFactory, rentalId)) {
                        throw BadRequestException("You can only end rentals you are involved in")
                    }
                    
                    val inactive = 0 // 0 = inactive, 1 = active
                    val updateRentalDTO = UpdateRentalDTO(inactive)
                    val updatedRental = serviceFactory.rentalService.update(rentalId, updateRentalDTO)
                    call.respond(updatedRental)
                }
                
                // Update a location of a rental by id
                put("/{rentalID}/locations/{locationID}") {
                    val rentalID = call.parameters["rentalID"]?.toIntOrNull() ?: throw BadRequestException("Invalid rental ID format")
                    val locationID = call.parameters["locationID"]?.toIntOrNull() ?: throw BadRequestException("Invalid location ID format")
                    
                    // Check if user can access this rental (renter or car owner)
                    if (!call.canAccessRental(serviceFactory, rentalID)) {
                        throw BadRequestException("You can only update locations of rentals you are involved in")
                    }
                    
                    // Check if the location belongs to this rental
                    val rental = serviceFactory.rentalService.getSingle(rentalID)
                    if (rental.startRentalLocation.rentalLocationID != locationID && rental.endRentalLocation.rentalLocationID != locationID) {
                        throw NotFoundException("Location does not belong to this rental")
                    }
                    
                    val updateRentalLocationDTO = call.receive<UpdateRentalLocationDTO>()
                    val updatedRentalLocation = serviceFactory.rentalLocationService.update(locationID, updateRentalLocationDTO)
                    call.respond(updatedRentalLocation)
                }

                // Delete a rental by id
                delete("/{id}") {
                    val rentalId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid rental ID format")

                    // Check if user can access this rental (renter or car owner)
                    if (!call.canAccessRental(serviceFactory, rentalId)) {
                        throw BadRequestException("You can only delete rentals you are involved in")
                    }

                    val success = serviceFactory.rentalService.delete(rentalId)
                    if (!success) throw NotFoundException("Rental not found")

                    call.respond(true)
                }
            }
        }
    }
}