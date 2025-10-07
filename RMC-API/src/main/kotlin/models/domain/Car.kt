package com.profgroep8.models.domain

import com.profgroep8.models.dto.CarDTO
import com.profgroep8.models.entity.Cars
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

enum class FuelType(val value: Int) {
    GASOLINE(0),
    DIESEL(1),
    ELECTRIC(2),
    HYBRID(3)
}

class Car(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Car>(Cars)

    var licensePlate by Cars.licensePlate
    var brand by Cars.brand
    var model by Cars.model
    var year by Cars.year
    var fuelType by Cars.fuelType
    var price by Cars.price
    var userId by Cars.userId
}

fun Car.toCarDTO() = CarDTO(
    this.id.value,
    this.licensePlate,
    this.brand,
    this.model,
    this.year,
    this.fuelType,
    this.price,
    this.userId
)
