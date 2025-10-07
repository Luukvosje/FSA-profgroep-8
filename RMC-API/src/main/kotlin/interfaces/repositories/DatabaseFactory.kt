package com.profgroep8.interfaces.repositories

import com.profgroep8.models.domain.Car
import com.profgroep8.models.domain.User

interface DatabaseFactory {
    val carRepository: GenericRepository<Car>
    val userRepository: UserRepository<User>
}