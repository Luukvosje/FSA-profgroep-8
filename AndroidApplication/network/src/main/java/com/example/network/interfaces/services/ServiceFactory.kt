package com.example.network.interfaces.services

import com.example.network.models.domain.User

interface ServiceFactory {
    val carService: CarService
    val rentalService: RentalService

    val userService: UserService
    fun setToken(token: String?)
    fun setUser(user: User)
}