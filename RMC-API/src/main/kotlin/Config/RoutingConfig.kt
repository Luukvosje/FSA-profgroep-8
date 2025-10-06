package com.profgroep8.Config

import com.profgroep8.Controller.Car.CarController
import com.profgroep8.Controller.Car.carRoutes
import com.profgroep8.Data.Repositories.CarRepository
import com.profgroep8.Service.CarService
import io.ktor.server.application.*

fun Application.configureRouting() {
    val carRepository = CarRepository()
    val carService = CarService(carRepository)
    val carController = CarController(carService)
    carRoutes(carController)

}
