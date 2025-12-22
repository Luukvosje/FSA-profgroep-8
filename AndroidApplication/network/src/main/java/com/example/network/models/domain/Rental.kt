package com.example.network.models.domain

data class Rental (
    val rentalID: Int,
    val userID: Int,
    val carID: Int,
    val startRentalLocationID: Int,
    val endRentalLocationID: Int,
    val state: Int,
)

data class RentalWithLocations(
    val rentalID: Int,
    val userID: Int,
    val carID: Int,
    val startRentalLocation: RentalLocation,
    val endRentalLocation: RentalLocation,
    val state: Int,
)