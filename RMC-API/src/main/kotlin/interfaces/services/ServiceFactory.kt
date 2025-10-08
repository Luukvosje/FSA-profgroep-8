package com.profgroep8.interfaces.services

interface ServiceFactory {
    val carService: CarService
    val userService: UserService
}
