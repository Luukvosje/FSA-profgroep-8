package com.example.network.models.remote

import com.example.network.models.domain.Rental
import com.example.network.models.domain.RentalWithLocations
import kotlinx.serialization.Serializable

@Serializable
data class RemoteRental(
    val rentalID: Int,
    val userID: Int,
    val carID: Int,
    val startRentalLocationID: Int,
    val endRentalLocationID: Int,
    val state: Int,
)

@Serializable
data class RemoteRentalWithLocations(
    val rentalID: Int,
    val userID: Int,
    val carID: Int,
    val startRentalLocation: RemoteRentalLocation,
    val endRentalLocation: RemoteRentalLocation,
    val state: Int,
)

fun RemoteRental.toDomainRental(): Rental {
    return Rental(
        rentalID = rentalID,
        userID = userID,
        carID = carID,
        startRentalLocationID = startRentalLocationID,
        endRentalLocationID = endRentalLocationID,
        state = state,
    )
}

fun RemoteRentalWithLocations.toDomainRental(): RentalWithLocations {
    return RentalWithLocations(
        rentalID = rentalID,
        userID = userID,
        carID = carID,
        startRentalLocation = startRentalLocation.toDomainRentalLocation(),
        endRentalLocation = endRentalLocation.toDomainRentalLocation(),
        state = state,
    )
}

@Serializable
data class CreateRentalDTO(
    val carID: Int,
    val startLocation: CreateRentalLocationDTO,
    val endLocation: CreateRentalLocationDTO
)

@Serializable
data class UpdateRentalDTO(
    val state: Int,
)