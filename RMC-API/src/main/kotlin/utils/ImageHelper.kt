package com.profgroep8.utils

import com.profgroep8.exceptions.UnauthorizedException
import com.profgroep8.interfaces.services.ServiceFactory
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import java.io.File

fun findImageOrThrow(
    carID: Int,
    serviceFactory: ServiceFactory,
    userID: Int
): File {

    val car = serviceFactory.carService.getSingle(carID)
        ?: throw NotFoundException("Car not found")

    if (car.userID != userID) {
        throw UnauthorizedException()
    }

    val uploadDir = File("uploads/cars")

    if (!uploadDir.exists() || !uploadDir.isDirectory) {
        throw NotFoundException("Image not found")
    }

    val imageFile = uploadDir.listFiles()
        ?.firstOrNull { it.name.startsWith("car_$carID.") && it.isFile }

    return imageFile ?: throw NotFoundException("Image not found")
}

//fun findImageOrThrowd(
//    carID: Int,
//    serviceFactory: ServiceFactory,
//    userID: Int
//): File {
//
//    val car = serviceFactory.carService.getSingle(carID)
//        ?: throw NotFoundException("Car not found")
//
//    if (car.userID != userID) {
//        throw UnauthorizedException()
//    }
//
//    val uploadDir = File("uploads/cars")
//
//    val imageFile = uploadDir.listFiles()
//        ?.firstOrNull { it.name.startsWith("car_$carID.") }
//        ?: throw NotFoundException("Image not found")
//
//    return imageFile
//}