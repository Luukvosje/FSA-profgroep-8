package com.profgroep8.models.dto

import com.profgroep8.models.domain.FuelType

data class CarDTO(
    val id: Int,
    val licensePlate: String,
    val brand: String,
    val model: String,
    val year: Int,
    val fuelType: FuelType,
    val price: Int,
    val userId: Int
)

data class CreateCarDTO(
    val licensePlate: String,
    val brand: String,
    val model: String,
    val year: Int,
    val fuelType: FuelType,
    val price: Int,
)