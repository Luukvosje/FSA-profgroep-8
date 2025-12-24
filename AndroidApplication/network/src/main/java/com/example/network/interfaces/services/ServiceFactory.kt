package com.example.network.interfaces.services

interface ServiceFactory {
    val carService: CarService
    fun setToken(token: String?)
}