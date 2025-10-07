package com.profgroep8.services

import com.profgroep8.repositories.CarRepository

interface CarService {
    val carRepository: CarRepository;
}

class CarServiceImpl: CarService {
    override val carRepository = CarRepository()

}