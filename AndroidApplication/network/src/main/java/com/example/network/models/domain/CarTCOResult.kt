package com.example.network.models.domain

data class CarTCOResult(
    val car: Car,
    val tco: Double,
    val costPerKm: Double,
)