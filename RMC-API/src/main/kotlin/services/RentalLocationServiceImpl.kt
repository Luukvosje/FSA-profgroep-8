package com.profgroep8.services

import com.profgroep8.interfaces.services.RentalLocationService
import com.profgroep8.models.dto.CreateRentalLocationDTO
import com.profgroep8.models.dto.RentalLocationDTO
import com.profgroep8.models.dto.UpdateRentalLocationDTO
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException

class RentalLocationServiceImpl(val serviceFactoryImpl: ServiceFactoryImpl) : RentalLocationService {
    override fun getSingle(rentalLocationId: Int): RentalLocationDTO =
        serviceFactoryImpl.databaseFactory.rentalLocationRepository.getSingle(rentalLocationId)?.toRentalLocationDTO() ?: throw NotFoundException()

    override fun create(item: CreateRentalLocationDTO): RentalLocationDTO {
        val createdRentalLocation = serviceFactoryImpl.databaseFactory.rentalLocationRepository.create {
            date = item.date
            longitude = item.longitude
            latitude = item.latitude
        }

        return createdRentalLocation?.toRentalLocationDTO() ?: throw BadRequestException("Failed to create rental location")
    }

    override fun update(rentalLocationId: Int, item: UpdateRentalLocationDTO): RentalLocationDTO {
        val updatedRentalLocation = serviceFactoryImpl.databaseFactory.rentalLocationRepository.update(rentalLocationId) {
            date = item.date
            longitude = item.longitude
            latitude = item.latitude
        }

        return updatedRentalLocation?.toRentalLocationDTO() ?: throw NotFoundException("Rental location with id $rentalLocationId not found")
    }
}

