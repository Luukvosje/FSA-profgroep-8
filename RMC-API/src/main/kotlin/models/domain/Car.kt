package com.profgroep8.models.domain

data class Car(
    val carId: Int? = null,
    val licensePlate: String,
    val brand: String,
    val model: String,
    val year: Int,
    val fuelType: FuelType,
    val price: Int,
    val userId: Int
)

enum class FuelType(val value: Int) {
    GASOLINE(0),
    DIESEL(1),
    ELECTRIC(2),
    HYBRID(3)
}