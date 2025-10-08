package com.profgroep8.interfaces.services

import com.profgroep8.models.dto.CarDTO
import com.profgroep8.models.dto.CreateCarDTO
import com.profgroep8.models.dto.UpdateCarDTO

interface CarService {
    fun getAll(): List<CarDTO>
    fun getSingle(carId: Int): CarDTO
    fun create(item: CreateCarDTO): CarDTO
    fun update(carId: Int, item: UpdateCarDTO): CarDTO
    fun delete(carId: Int): Boolean
}