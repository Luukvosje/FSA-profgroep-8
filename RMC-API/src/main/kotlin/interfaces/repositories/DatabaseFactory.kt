package com.profgroep8.interfaces.repositories

import com.profgroep8.models.domain.Car

interface DatabaseFactory {
    val carRepository : GenericRepository<Car>
}