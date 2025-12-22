package com.example.network.models.domain

import java.time.LocalDateTime

data class RentalLocation (
    val rentalLocationID: Int,
    val date: LocalDateTime,
    val longitude: Float,
    val latitude: Float
)

data class RentalLocations (
    val startLocation: RentalLocation,
    val endLocation: RentalLocation
)