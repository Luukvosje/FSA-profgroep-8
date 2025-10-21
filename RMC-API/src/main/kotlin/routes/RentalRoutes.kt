package com.profgroep8.Controller.Rental

import com.profgroep8.interfaces.services.ServiceFactory
import com.profgroep8.models.dto.CreateRentalDTO
import com.profgroep8.models.dto.UpdateRentalDTO
import com.profgroep8.models.dto.UpdateRentalLocationDTO
import com.profgroep8.models.dto.RentalLocationsResponseDTO
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*

fun Application.rentalRoutes(serviceFactory: ServiceFactory) {

    routing {
        route("/rentals") {
            // Get all rentals
            get {
                val rentals = serviceFactory.rentalService.getAll()
                call.respond(rentals)
            }

            get("/{id}") {
                val rentalId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid rental ID format")
                val rental = serviceFactory.rentalService.getSingle(rentalId)
                call.respond(rental)
            }

            // Create new rental
            post {
                val createRentalDTO = call.receive<CreateRentalDTO>()
                val newRental = serviceFactory.rentalService.create(createRentalDTO)
                call.respond(newRental)
            }

            // Update rental by id
            put("/{id}") {
                val rentalId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid rental ID format")
                val updateRentalDTO = call.receive<UpdateRentalDTO>()

                val updatedRental = serviceFactory.rentalService.update(rentalId, updateRentalDTO)
                call.respond(updatedRental)
            }

            // Set rental to state inactive by id
            put("/{id}/end") {
                val rentalId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid rental ID format")
                val inactive = 0 // 0 = inactive, 1 = active
                val updateRentalDTO = UpdateRentalDTO(inactive)

                val updatedRental = serviceFactory.rentalService.update(rentalId, updateRentalDTO)
                call.respond(updatedRental)
            }

            // Get start and end location for rental by id
            get("/{id}/locations") {
                val rentalId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid rental ID format")
                val rental = serviceFactory.rentalService.getSingle(rentalId)
                
                val locationsResponse = RentalLocationsResponseDTO(
                    startLocation = rental.startRentalLocation,
                    endLocation = rental.endRentalLocation
                )
                
                call.respond(locationsResponse)
            }
            
            // Update a location of a rental by id
            put("/{rentalId}/locations/{locationId}") {
                val rentalId = call.parameters["rentalId"]?.toIntOrNull() ?: throw BadRequestException("Invalid rental ID format")
                val locationId = call.parameters["locationId"]?.toIntOrNull() ?: throw BadRequestException("Invalid location ID format")
                
                // Check if the location belongs to this rental
                val rental = serviceFactory.rentalService.getSingle(rentalId)
                if (rental.startRentalLocation.rentalLocationId != locationId && rental.endRentalLocation.rentalLocationId != locationId) {
                    throw NotFoundException("Location does not belong to this rental")
                }
                
                val updateRentalLocationDTO = call.receive<UpdateRentalLocationDTO>()
                val updatedRentalLocation = serviceFactory.rentalLocationService.update(locationId, updateRentalLocationDTO)
                call.respond(updatedRentalLocation)
            }

            // Delete a rental by id
            delete("/{id}") {
                val rentalId = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("Invalid rental ID format")

                val success = serviceFactory.rentalService.delete(rentalId)

                if (!success) throw NotFoundException("Rental not found")

                call.respond(true)
            }
        }
    }
}