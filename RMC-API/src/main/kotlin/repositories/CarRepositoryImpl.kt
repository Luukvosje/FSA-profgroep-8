package com.profgroep8.repositories

import com.profgroep8.Util.RdwImpl
import com.profgroep8.interfaces.repositories.CarRepository
import com.profgroep8.interfaces.repositories.GenericRepository
import com.profgroep8.models.domain.Car
import com.profgroep8.models.dto.CarDTO

class CarRepositoryImpl() : CarRepository, GenericRepository<Car> by GenericRepositoryImpl(Car) {
    override suspend fun findLicense(licensePlate: String): CarDTO? {
        TODO("not implemented")
    }

}