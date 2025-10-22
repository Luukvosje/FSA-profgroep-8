package com.profgroep8.interfaces.services

import com.profgroep8.models.dto.*

interface CarService {
    suspend fun findByLicense(licensePlate: String): CreateCarDTO?
    fun getAllCars(): List<CarDTO>
    fun getCarsByUserId(userID: Int): List<CarDTO>
    fun filterCars(filter: FilterCar): List<CarDTO>
    fun searchCars(keyword: String?): List<CarDTO>
    fun getSingle(carID: Int): CarDTO?
    fun calculateCar(request: CalculateCarRequestDTO, car: CarDTO): CalculateCarResponseDTO
    fun create(item: CreateCarDTO, userID: Int): CarDTO?
    fun update(carID: Int, item: UpdateCarDTO): CarDTO?
    fun delete(carID: Int): Boolean
}