package com.example.network.models.domain

import kotlinx.datetime.LocalDate


data class AvailableCar(
    val car: Car,
    val availableFrom: LocalDate?,
    val availableUntill: LocalDate?,
)

data class CarAvailabilityUi(
    val car: Car,
    val availableFrom: LocalDate?,
    val availableUntil: LocalDate?,
    val isAvailable: Boolean
)

data class Car (
    val carID: Int,
    val licensePlate: String,
    val brand: String,
    val model: String,
    val year: Int,
    val fuelType: CarFuelType,
    val price: Int,
    val userID: Int,
    val imageBytes: ByteArray?
)


