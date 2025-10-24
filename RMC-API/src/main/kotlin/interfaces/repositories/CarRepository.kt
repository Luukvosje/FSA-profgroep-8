package com.profgroep8.interfaces.repositories

import com.profgroep8.models.domain.Car
import com.profgroep8.models.dto.CarAvailability
import com.profgroep8.models.dto.CarDTO
import com.profgroep8.models.dto.FilterCar
import kotlinx.datetime.LocalDateTime

interface CarRepository : GenericRepository<Car> {
    fun getByUserId(userId: Int): List<CarDTO>
    fun filterCars(filter: FilterCar): List<CarDTO>
    fun searchCars(keyword: String?): List<CarDTO>
    fun getAvailableCars(startDate: LocalDateTime?): List<CarAvailability>
}