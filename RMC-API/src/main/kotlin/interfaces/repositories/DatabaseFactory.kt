package com.profgroep8.interfaces.repositories

import com.profgroep8.models.domain.Car
import com.profgroep8.models.domain.User

interface DatabaseFactory {
    val carRepository: CarRepository
    val userRepository: UserRepository<User>
}