package com.profgroep8.interfaces.services

import com.profgroep8.Util.RdwImpl

interface ServiceFactory {
    val rdwService: RdwImpl

    val carService: CarService
    val userService: UserService
    val rentalService: RentalService
    val rentalLocationService: RentalLocationService
}
