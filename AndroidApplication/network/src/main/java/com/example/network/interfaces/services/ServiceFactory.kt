package com.example.network.interfaces.services

interface ServiceFactory {
    val carService: CarService
    val rentalService: RentalService
    fun setToken(token: String?)
}