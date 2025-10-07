package com.profgroep8.repositories

import com.profgroep8.exceptions.ConflictException
import com.profgroep8.exceptions.NotFoundException
import com.profgroep8.models.domain.Car
import com.profgroep8.models.domain.FuelType
import com.profgroep8.models.domain.toCarDTO
import com.profgroep8.models.dto.CarDTO
import com.profgroep8.models.dto.CreateCarDTO
import com.profgroep8.models.entity.Cars
import com.profgroep8.models.entity.Users
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

class CarRepository() {
    fun getAll(): List<CarDTO> = transaction {
        Car.all()
           .map { it.toCarDTO() }
           .toList()
    }

    fun getCar(carId: Int): CarDTO = transaction {
        Car.findById(carId)
            ?.toCarDTO() ?: throw NotFoundException();
    }

    fun createCar(car: CreateCarDTO, userId: Int): CarDTO  = transaction {
        // Car already exists
        if (Car.find { Cars.licensePlate eq car.licensePlate }.any()) {
            throw ConflictException()
        }

        // User does not exist
//        if (User.findById(userId) == null) {
//            throw NotFoundException()
//        }

        Car.new {
            this.licensePlate = car.licensePlate
            this.price = car.price
            this.brand = car.brand
            this.model = car.model
            this.year = car.year
            this.fuelType = car.fuelType
            this.userId = userId
        }.toCarDTO()
    }

    fun deleteCar(carId: Int): Unit = transaction {
        Car.findById(carId)
            ?.delete() ?: throw NotFoundException();
    }
}