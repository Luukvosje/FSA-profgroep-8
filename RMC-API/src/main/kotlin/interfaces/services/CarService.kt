package com.profgroep8.interfaces.services

import com.profgroep8.models.dto.CarDTO
import com.profgroep8.models.dto.CreateCarDTO

interface CarService {
    fun getAll(): List<CarDTO>
    fun getSingle(carId: Int): CarDTO
    fun create(item: CreateCarDTO): CarDTO
}