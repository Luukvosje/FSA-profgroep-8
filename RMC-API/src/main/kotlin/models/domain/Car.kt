package com.profgroep8.models.domain

import com.profgroep8.models.dto.CalculateCarRequestDTO
import com.profgroep8.models.dto.CalculateCarResponseDTO
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

    fun calculateTCO(request: CalculateCarRequestDTO): CalculateCarResponseDTO {
        val depreciation: Double
        val maintenance: Double
        val tax: Double
        val energyCost: Double

        when (FuelType.fromCode(this.fuelType)) {
            FuelType.GASOLINE -> {
                depreciation = 3000.0
                maintenance = 700.0
                tax = 600.0
                val fuelConsumptionPer100Km = 6.5
                val fuelPrice = 2.10
                energyCost = (fuelConsumptionPer100Km / 100.0) * fuelPrice * request.standardKmPerYear
            }

            FuelType.DIESEL -> {
                depreciation = 3200.0
                maintenance = 750.0
                tax = 800.0
                val fuelConsumptionPer100Km = 5.5
                val fuelPrice = 1.95
                energyCost = (fuelConsumptionPer100Km / 100.0) * fuelPrice * request.standardKmPerYear
            }

            FuelType.ELECTRIC -> {
                depreciation = 2500.0
                maintenance = 400.0
                tax = 0.0
                val consumptionPer100Km = 18.0
                val electricityPrice = 0.35
                energyCost = (consumptionPer100Km / 100.0) * electricityPrice * request.standardKmPerYear
            }

            FuelType.HYBRID -> {
                depreciation = 2800.0
                maintenance = 600.0
                tax = 400.0
                val fuelConsumptionPer100Km = 4.0
                val fuelPrice = 2.05
                val electricityCost = 0.05 * request.standardKmPerYear
                energyCost = (fuelConsumptionPer100Km / 100.0) * fuelPrice * request.standardKmPerYear + electricityCost
            }
        }

        val totalCost = depreciation + maintenance + tax + energyCost
        val costPerKm = totalCost / request.standardKmPerYear

        return CalculateCarResponseDTO(
            carId = id.value,
            fuelType = fuelType,
            tco = totalCost,
            costPerKm = costPerKm
        )
    }

}

