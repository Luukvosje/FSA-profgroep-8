package com.example.network.models.domain

data class User(
    val userID: Int,
    val fullName: String,
    val email: String,
    val phone: String,
    val address: String,
    val zipcode: String,
    val city: String,
    val countryISO: String,
    val bonusPoints: Int,
    val token: String
)