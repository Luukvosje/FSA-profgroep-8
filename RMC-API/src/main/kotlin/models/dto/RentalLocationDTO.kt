package com.profgroep8.models.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class RentalLocationDTO(
    val rentalLocationId: Int,
    val date: LocalDateTime,
    val longitude: Float,
    val latitude: Float
)

@Serializable
data class CreateRentalLocationDTO(
    val date: LocalDateTime,
    val longitude: Float,
    val latitude: Float
)

@Serializable
data class UpdateRentalLocationDTO(
    val date: LocalDateTime,
    val longitude: Float,
    val latitude: Float
)

