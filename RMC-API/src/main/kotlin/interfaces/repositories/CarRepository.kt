package com.profgroep8.interfaces.repositories

import com.profgroep8.models.domain.Car
import com.profgroep8.models.domain.User
import com.profgroep8.models.dto.CarDTO

interface CarRepository : GenericRepository<Car> {
    suspend fun findLicense(licensePlate: String): CarDTO?
    fun getByUserId(userId: Int): List<CarDTO>?
}