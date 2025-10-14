package com.profgroep8.models.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import kotlinx.serialization.Serializable

@Serializable
data class CarDTO(
    val carId: Int,
    val licensePlate: String,
    val brand: String,
    val model: String,
    val year: Int,
    val fuelType: Int,
    val price: Int,
    val userId: Int
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

    @JsonIgnore
    var carId: Int,
)

@Serializable
data class CalculateCarResponseDTO(
    val carId: Int,
    val fuelType: Int,
    val tco: Double,
    val costPerKm: Double,
)