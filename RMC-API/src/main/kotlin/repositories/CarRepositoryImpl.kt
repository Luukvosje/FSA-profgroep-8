package com.profgroep8.repositories

import com.profgroep8.interfaces.repositories.CarRepository
import com.profgroep8.interfaces.repositories.GenericRepository
import com.profgroep8.models.domain.Car

class CarRepositoryImpl(
    genericRepository: GenericRepository<Car>
) : CarRepository, GenericRepository<Car> by genericRepository {
    override fun findLicense() {
        TODO("Not yet implemented")
    }
}