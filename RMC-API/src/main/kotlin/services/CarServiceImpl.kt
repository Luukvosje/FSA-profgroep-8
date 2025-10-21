package com.profgroep8.services

import com.profgroep8.interfaces.services.CarService
import com.profgroep8.models.dto.*
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.transactions.transaction

class CarServiceImpl(val serviceFactoryImpl: ServiceFactoryImpl) : CarService {
    override fun getAll() =
        serviceFactoryImpl.databaseFactory.carRepository.getAll().map { it.toCarDTO() }

    override fun getSingle(carId: Int): CarDTO =
        serviceFactoryImpl.databaseFactory.carRepository.getSingle(carId)?.toCarDTO() ?: throw NotFoundException()

    override fun create(item: CreateCarDTO): CarDTO {
        val plate = item.licensePlate.lowercase().replace("-", "").trim()

        val createdCar = serviceFactoryImpl.databaseFactory.carRepository.create {
            this.licensePlate = plate
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
        serviceFactoryImpl.rdwClient.getCar(licensePlate)

    override fun getCarsByUserId(userId: Int): List<CarDTO>? =
        serviceFactoryImpl.databaseFactory.carRepository.getByUserId(userId)

    override fun calculateCar(request: CalculateCarRequestDTO): CalculateCarResponseDTO? =
        try {
            val car = serviceFactoryImpl.databaseFactory.carRepository.getSingle(request.carId)
                ?: throw BadRequestException("Unexpected error")

            car.calculateTCO(request.standardKmPerYear)
        } catch (e: Exception) {
            throw BadRequestException("Car could not be found")
        }

    override fun filterCar(filter: FilterCar): List<CarDTO> =
        serviceFactoryImpl.databaseFactory.carRepository.filterCars(filter)
            ?: throw BadRequestException("Unexpected error")

    override fun searchCars(keyword: String): List<CarDTO> =
        serviceFactoryImpl.databaseFactory.carRepository.searchCars(keyword)
            ?: throw BadRequestException("Unexpected error")
}