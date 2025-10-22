package com.profgroep8.models.domain

import com.profgroep8.models.dto.CarDTO
import com.profgroep8.models.entity.CarEntity
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

enum class FuelType(val code: Int) {
    GASOLINE(0),
    DIESEL(1),
    ELECTRIC(2),
    HYBRID(3);

    companion object {
        fun fromCode(code: Int): FuelType {
            return values().find { it.code == code }
                ?: throw IllegalArgumentException("Invalid fuelType code: $code")
        }
    }
}

class Car(carId: EntityID<Int>) : IntEntity(carId) {
    companion object : IntEntityClass<Car>(CarEntity)

    var licensePlate by CarEntity.licensePlate
    var brand by CarEntity.brand
    var model by CarEntity.model
    var year by CarEntity.year
    var fuelType by CarEntity.fuelType
    var price by CarEntity.price
    var userId by CarEntity.userId

    fun toCarDTO(): CarDTO {
        return CarDTO(
            this.id.value,
            this.licensePlate,
            this.brand,
            this.model,
            this.year,
            this.fuelType,
            this.price,
            this.userId
        )
    }
}

