package com.profgroep8.services

import com.profgroep8.interfaces.services.RentalService
import com.profgroep8.models.dto.CreateRentalDTO
import com.profgroep8.models.dto.RentalWithLocationsDTO
import com.profgroep8.models.dto.UpdateRentalDTO
import io.ktor.server.plugins.*

class RentalServiceImpl(val serviceFactoryImpl: ServiceFactoryImpl): RentalService {
    override fun getAll() =
        serviceFactoryImpl.databaseFactory.rentalRepository.getAll().map { rental ->
            val startLocation = serviceFactoryImpl.databaseFactory.rentalLocationRepository.getSingle(rental.startRentalLocationId)
                ?: throw IllegalStateException("Start rental location not found")
            val endLocation = serviceFactoryImpl.databaseFactory.rentalLocationRepository.getSingle(rental.endRentalLocationId)
                ?: throw IllegalStateException("End rental location not found")
            rental.toRentalWithLocationsDTO(startLocation, endLocation)
        }

    override fun getSingle(rentalId: Int): RentalWithLocationsDTO {
        val rental = serviceFactoryImpl.databaseFactory.rentalRepository.getSingle(rentalId) ?: throw NotFoundException()
        val startLocation = serviceFactoryImpl.databaseFactory.rentalLocationRepository.getSingle(rental.startRentalLocationId)
            ?: throw IllegalStateException("Start rental location not found")
        val endLocation = serviceFactoryImpl.databaseFactory.rentalLocationRepository.getSingle(rental.endRentalLocationId)
            ?: throw IllegalStateException("End rental location not found")
        return rental.toRentalWithLocationsDTO(startLocation, endLocation)
    }

    override fun create(item: CreateRentalDTO): RentalWithLocationsDTO {
        // Check if car is already rented
        val existingRental = serviceFactoryImpl.databaseFactory.rentalRepository.getActiveRentalByCar(item.carId)
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
            this.userId = item.userId
            this.carId = item.carId
            this.startRentalLocationId = startLocation.id.value
            this.endRentalLocationId = endLocation.id.value
            this.state = 1
        }

        return createdRental?.toRentalWithLocationsDTO(startLocation, endLocation) ?: throw BadRequestException("Failed to create rental")
    }

    override fun update(rentalId: Int, item: UpdateRentalDTO): RentalWithLocationsDTO {
        val updatedRental = serviceFactoryImpl.databaseFactory.rentalRepository.update(rentalId) {
            this.state = item.state
        }

        return updatedRental?.let { rental ->
            val startLocation = serviceFactoryImpl.databaseFactory.rentalLocationRepository.getSingle(rental.startRentalLocationId)
                ?: throw IllegalStateException("Start rental location not found")
            val endLocation = serviceFactoryImpl.databaseFactory.rentalLocationRepository.getSingle(rental.endRentalLocationId)
                ?: throw IllegalStateException("End rental location not found")
            rental.toRentalWithLocationsDTO(startLocation, endLocation)
        } ?: throw BadRequestException("Unexpected error")
    }

    override fun delete(rentalId: Int): Boolean {
        val rental = serviceFactoryImpl.databaseFactory.rentalRepository.getSingle(rentalId) ?: return false

        // Rental is automatically deleted by cascade
        val startLocationDeleted = serviceFactoryImpl.databaseFactory.rentalLocationRepository.delete(rental.startRentalLocationId)
        val endLocationDeleted = serviceFactoryImpl.databaseFactory.rentalLocationRepository.delete(rental.endRentalLocationId)
        
        return startLocationDeleted && endLocationDeleted
    }
}