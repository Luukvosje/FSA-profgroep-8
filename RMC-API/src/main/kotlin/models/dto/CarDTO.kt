package com.profgroep8.models.dto

import kotlinx.datetime.LocalDateTime
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
    val sortOrder: String? = null,
    val licensePlate: String? = null,
    val brand: String? = null,
    val model: String? = null,
    val year: Int? = null,
    val fuelType: Int? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
) {
    fun ToSearchValues(): FilterCar = this.copy(
        licensePlate = this.licensePlate?.let { it.lowercase().replace("-", "").trim() },
        brand = this.brand?.let { it.lowercase().trim() },
        model = this.model?.let { it.lowercase().trim() },
    )
}

enum class FilterSortOrder(val sortString: String) {
    Year("year"),
    FuelType("fuelType"),
    Model("model"),
    Brand("brand"),
    price("price"),
    nothing("");

    companion object {
        fun fromString(sortString: String): FilterSortOrder {
            return values().find { it.sortString == sortString }
                ?: nothing
        }
    }
}

@Serializable
data class Availability(
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
)