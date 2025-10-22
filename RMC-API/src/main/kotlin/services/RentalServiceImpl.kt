package com.profgroep8.services

import com.profgroep8.interfaces.services.RentalService
import com.profgroep8.models.dto.CreateRentalDTO
import com.profgroep8.models.dto.RentalWithLocationsDTO
import com.profgroep8.models.dto.UpdateRentalDTO
import io.ktor.server.plugins.*
import io.ktor.server.plugins.NotFoundException

class RentalServiceImpl(val serviceFactoryImpl: ServiceFactoryImpl): RentalService {
    override fun getAll() =
        serviceFactoryImpl.databaseFactory.rentalRepository.getAll().map { rental ->
            val startLocation = serviceFactoryImpl.databaseFactory.rentalLocationRepository.getSingle(rental.startRentalLocationID)
                ?: throw IllegalStateException("Start rental location not found")
            val endLocation = serviceFactoryImpl.databaseFactory.rentalLocationRepository.getSingle(rental.endRentalLocationID)
                ?: throw IllegalStateException("End rental location not found")
            rental.toRentalWithLocationsDTO(startLocation, endLocation)
        }

    override fun getSingle(rentalID: Int): RentalWithLocationsDTO {
        val rental = serviceFactoryImpl.databaseFactory.rentalRepository.getSingle(rentalID) ?: throw NotFoundException()
        val startLocation = serviceFactoryImpl.databaseFactory.rentalLocationRepository.getSingle(rental.startRentalLocationID)
            ?: throw IllegalStateException("Start rental location not found")
        val endLocation = serviceFactoryImpl.databaseFactory.rentalLocationRepository.getSingle(rental.endRentalLocationID)
            ?: throw IllegalStateException("End rental location not found")
        return rental.toRentalWithLocationsDTO(startLocation, endLocation)
    }

    override fun create(item: CreateRentalDTO, userID: Int): RentalWithLocationsDTO {
        // Check if car is already rented
        val existingRental = serviceFactoryImpl.databaseFactory.rentalRepository.getActiveRentalByCar(item.carID)
        if (existingRental != null) {
            throw BadRequestException("Car is currently rented")
        }

        val startLocation = serviceFactoryImpl.databaseFactory.rentalLocationRepository.create {
            date = item.startLocation.date
            longitude = item.startLocation.longitude
            latitude = item.startLocation.latitude
        } ?: throw BadRequestException("Failed to create start location")

        val endLocation = serviceFactoryImpl.databaseFactory.rentalLocationRepository.create {
            date = item.endLocation.date
            longitude = item.endLocation.longitude
            latitude = item.endLocation.latitude
        } ?: throw BadRequestException("Failed to create end location")

        val createdRental = serviceFactoryImpl.databaseFactory.rentalRepository.create {
            this.userID = userID
            this.carID = item.carID
            this.startRentalLocationID = startLocation.id.value
            this.endRentalLocationID = endLocation.id.value
            this.state = 1
        }

        return createdRental?.toRentalWithLocationsDTO(startLocation, endLocation) ?: throw BadRequestException("Failed to create rental")
    }

    override fun update(rentalID: Int, item: UpdateRentalDTO): RentalWithLocationsDTO {
        val updatedRental = serviceFactoryImpl.databaseFactory.rentalRepository.update(rentalID) {
            this.state = item.state
        }

        return updatedRental?.let { rental ->
            val startLocation = serviceFactoryImpl.databaseFactory.rentalLocationRepository.getSingle(rental.startRentalLocationID)
                ?: throw IllegalStateException("Start rental location not found")
            val endLocation = serviceFactoryImpl.databaseFactory.rentalLocationRepository.getSingle(rental.endRentalLocationID)
                ?: throw IllegalStateException("End rental location not found")
            rental.toRentalWithLocationsDTO(startLocation, endLocation)
        } ?: throw NotFoundException("Rental not found")
    }

    override fun delete(rentalID: Int): Boolean {
        val rental = serviceFactoryImpl.databaseFactory.rentalRepository.getSingle(rentalID) ?: return false

        // Rental is automatically deleted by cascade
        val startLocationDeleted = serviceFactoryImpl.databaseFactory.rentalLocationRepository.delete(rental.startRentalLocationID)
        val endLocationDeleted = serviceFactoryImpl.databaseFactory.rentalLocationRepository.delete(rental.endRentalLocationID)
        
        return startLocationDeleted && endLocationDeleted
    }
}