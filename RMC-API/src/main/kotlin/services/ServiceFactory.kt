package com.profgroep8.services

interface ServiceFactory {
    val rentalService: RentalService
    val carService: CarService
    val userService: UserService
}

class ServiceFactoryImpl(): ServiceFactory {
    override val rentalService: RentalService = RentalServiceImpl()
    override val carService: CarService = CarServiceImpl()
    override val userService: UserService = UserServiceImpl()
}