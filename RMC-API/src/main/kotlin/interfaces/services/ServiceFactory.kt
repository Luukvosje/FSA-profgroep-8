package com.profgroep8.interfaces.services

import com.profgroep8.Util.RdwClient

interface ServiceFactory {
    val rdwClient: RdwClient

    val carService: CarService
    val userService: UserService
    val rentalService: RentalService
    val rentalLocationService: RentalLocationService
}
