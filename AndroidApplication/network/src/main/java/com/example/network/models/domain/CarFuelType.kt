package com.example.network.models.domain

sealed class CarFuelType(val displayName: String) {
    object Gasoline: CarFuelType("Gasoline")
    object Diesel: CarFuelType("Diesel")
    object Electric: CarFuelType("Electric")
    object Hybrid: CarFuelType("Hybrid")
    object Unknown: CarFuelType("Unknown")
}