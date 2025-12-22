package com.example.network.models.remote

import com.example.network.models.domain.RentalLocation
import com.example.network.models.domain.RentalLocations
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import kotlin.Int

@Serializable
data class RemoteRentalLocation(
    val rentalLocationID: Int,
    val date: String,
    val longitude: Float,
    val latitude: Float
)

@Serializable
data class RemoteRentalLocations(
    val startLocation: RemoteRentalLocation,
    val endLocation: RemoteRentalLocation
)

fun RemoteRentalLocation.toDomainRentalLocation(): RentalLocation {
    return RentalLocation(
        rentalLocationID = rentalLocationID,
        date = LocalDateTime.parse(date),
        longitude = longitude,
        latitude = latitude,
    )
}

fun RemoteRentalLocations.toDomainRentalLocations(): RentalLocations {
    return RentalLocations(
        startLocation = startLocation.toDomainRentalLocation(),
        endLocation = endLocation.toDomainRentalLocation()
    )
}

@Serializable
data class CreateRentalLocationDTO(
    val date: String,
    val longitude: Float,
    val latitude: Float
)

@Serializable
data class UpdateRentalLocationDTO(
    val date: String,
    val longitude: Float,
    val latitude: Float
)