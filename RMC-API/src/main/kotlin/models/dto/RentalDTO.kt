package com.profgroep8.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class RentalDTO(
    val rentalId: Int,
    val userId: Int,
    val carId: Int,
    val startRentalLocationId: Int,
    val endRentalLocationId: Int,
    val state: Int,
)

@Serializable
data class CreateRentalDTO(
    val userId: Int,
    val carId: Int,
    val startLocation: CreateRentalLocationDTO,
    val endLocation: CreateRentalLocationDTO
)

@Serializable
data class UpdateRentalDTO(
    val state: Int,
)

@Serializable
data class RentalWithLocationsDTO(
    val rentalId: Int,
    val userId: Int,
    val carId: Int,
    val startRentalLocation: RentalLocationDTO,
    val endRentalLocation: RentalLocationDTO,
    val state: Int,
)

@Serializable
data class RentalLocationsResponseDTO(
    val startLocation: RentalLocationDTO,
    val endLocation: RentalLocationDTO
)