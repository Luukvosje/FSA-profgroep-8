package com.profgroep8.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val userId: Int,
    val fullName: String,
    val email: String,
    val phone: String,
    val address: String,
    val zipcode: String,
    val city: String,
    val countryISO: String,
    val points: Int
)

@Serializable
data class CreateUserDTO(
    val fullName: String,
    val email: String,
    val password: String,
    val phone: String,
    val address: String,
    val zipcode: String,
    val city: String,
    val countryISO: String
)
