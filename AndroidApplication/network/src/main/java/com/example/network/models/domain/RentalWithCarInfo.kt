package com.example.network.models.domain

data class RentalWithCarInfo(
    val rental: RentalWithLocations,
    val car: Car?
)