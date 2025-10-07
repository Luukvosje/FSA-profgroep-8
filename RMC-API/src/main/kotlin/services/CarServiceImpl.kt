package com.profgroep8.services

import com.profgroep8.exceptions.NotFoundException
import com.profgroep8.interfaces.services.CarService
import com.profgroep8.models.dto.CarDTO
import com.profgroep8.models.dto.CreateCarDTO

class CarServiceImpl(val serviceFactoryImpl: ServiceFactoryImpl): CarService {
    override fun getAll() =
        serviceFactoryImpl.databaseFactory.carRepository.getAll().map { it.toCarDTO() }

    override fun getSingle(carId: Int): CarDTO =
        serviceFactoryImpl.databaseFactory.carRepository.getSingle(carId)?.toCarDTO() ?: throw NotFoundException()

    override fun create(item: CreateCarDTO): CarDTO {
        val newCar = serviceFactoryImpl.databaseFactory.carRepository.create {
            this.licensePlate = item.licensePlate
            this.brand = item.brand
            this.model = item.model
            this.brand = item.brand
            this.price = item.price
            this.year = item.year
            this.fuelType = item.fuelType
            this.userId = 1
        }

        return newCar.toCarDTO()
    }
}