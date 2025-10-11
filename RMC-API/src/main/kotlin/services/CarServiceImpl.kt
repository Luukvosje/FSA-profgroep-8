package com.profgroep8.services

import com.profgroep8.interfaces.services.CarService
import com.profgroep8.models.dto.CarDTO
import com.profgroep8.models.dto.CreateCarDTO
import com.profgroep8.models.dto.UpdateCarDTO
import io.ktor.server.plugins.*

class CarServiceImpl(val serviceFactoryImpl: ServiceFactoryImpl) : CarService {
    override fun getAll() =
        serviceFactoryImpl.databaseFactory.carRepository.getAll().map { it.toCarDTO() }

    override fun getSingle(carId: Int): CarDTO =
        serviceFactoryImpl.databaseFactory.carRepository.getSingle(carId)?.toCarDTO() ?: throw NotFoundException()

    override fun create(item: CreateCarDTO): CarDTO {
        val createdCar = serviceFactoryImpl.databaseFactory.carRepository.create {
            this.licensePlate = item.licensePlate
            this.brand = item.brand
            this.model = item.model
            this.brand = item.brand
            this.price = item.price
            this.year = item.year
            this.fuelType = item.fuelType
            this.userId = 1
        }

        return createdCar?.toCarDTO() ?: throw BadRequestException("Unexpected error")
    }

    override fun update(carId: Int, item: UpdateCarDTO): CarDTO {
        val updatedCar = serviceFactoryImpl.databaseFactory.carRepository.update(carId, {
            this.licensePlate = item.licensePlate
            this.brand = item.brand
            this.model = item.model
            this.brand = item.brand
            this.price = item.price
            this.year = item.year
            this.fuelType = item.fuelType
            this.userId = 1
        })

        return updatedCar?.toCarDTO() ?: throw BadRequestException("Unexpected error")
    }

    override fun delete(carId: Int): Boolean =
        serviceFactoryImpl.databaseFactory.carRepository.delete(carId)

    override suspend fun findByLicense(licensePlate: String): CreateCarDTO? =
        serviceFactoryImpl.rdwService.getCar(licensePlate)
}