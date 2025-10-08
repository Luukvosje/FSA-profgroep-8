package com.profgroep8.repositories

import com.profgroep8.interfaces.repositories.CarRepository
import com.profgroep8.interfaces.repositories.GenericRepository
import com.profgroep8.models.domain.Car

class CarRepositoryImpl() : CarRepository, GenericRepository<Car> by GenericRepositoryImpl(Car) {
    override fun findLicense() {
        TODO("Not yet implemented")
    }
}