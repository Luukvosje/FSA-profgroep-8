package com.example.network.services

import com.example.network.interfaces.services.CarService
import io.ktor.client.call.body
import io.ktor.client.request.get

internal class CarServiceImpl : BaseServiceImpl(), CarService {
    override suspend fun getAllCars() {
        return client.get("cars").body()
    }
}
