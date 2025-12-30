package com.profgroep8.utils

import com.profgroep8.exceptions.UnauthorizedException
import com.profgroep8.interfaces.services.ServiceFactory
import com.profgroep8.services.ServiceFactoryImpl
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import java.io.File

fun FindImage(carID: Int?, serviceFactory: ServiceFactory, userID: Int): File {
    val _carId = carID
        ?: throw BadRequestException("Invalid car ID")

    val car = serviceFactory.carService.getSingle(_carId)
        ?: throw NotFoundException("Car not found")

    if (car.userID != userID) {
        throw UnauthorizedException()
    }

    val uploadDir = File("uploads/cars")
    return uploadDir
}