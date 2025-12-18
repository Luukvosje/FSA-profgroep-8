package com.example.network.models.domain

import kotlinx.serialization.Serializable

data class Car (
    val carID: Int,
    val licensePlate: String,
    val brand: String,
    val model: String,
    val year: Int,
    val fuelType: CarFuelType,
    val price: Int,
    val userID: Int,
)

