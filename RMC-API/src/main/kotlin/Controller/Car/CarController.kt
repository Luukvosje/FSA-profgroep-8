package com.profgroep8.Controller.Car

import com.profgroep8.Service.CarService
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import org.postgresql.core.TypeInfo


class CarController(private val carService: CarService) {

    //example call
    suspend fun getAllCars(call: ApplicationCall) {
//        val cars = carService.getAllCars()
//        call.respond(cars)
        call.respond("aap", )
    }
}
