package com.profgroep8.models.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import kotlinx.serialization.Serializable

@Serializable
data class CarDTO(
    val carID: Int,
    val licensePlate: String,
    val brand: String,
    val model: String,
    val year: Int,
    val fuelType: Int,
    val price: Int,
    val userID: Int
)

@Serializable
data class CreateCarDTO(
    val licensePlate: String,
    val brand: String,
    val model: String,
    val year: Int,
    val fuelType: Int,
    val price: Int,
)

@Serializable
data class UpdateCarDTO(
    val licensePlate: String,
    val brand: String,
    val model: String,
    val year: Int,
    val fuelType: Int,
    val price: Int,
)

@Serializable
data class CalculateCarRequestDTO(
    val standardKmPerYear: Double,
)

@Serializable
data class CalculateCarResponseDTO(
    val car: CarDTO,
    val tco: Double,
    val costPerKm: Double,
)

@Serializable
data class FilterCar(
    val licensePlate: String? = null,
    val brand: String? = null,
    val model: String? = null,
    val year: Int? = null,
    val fuelType: Int? = null,
    val price: Int? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
) {
    fun ToSearchValues(): FilterCar = this.copy(
        licensePlate = this.licensePlate?.let { it.lowercase().replace("-", "").trim() },
        brand = this.brand?.let { it.lowercase().trim() },
        model = this.model?.let { it.lowercase().trim() },
    )
}