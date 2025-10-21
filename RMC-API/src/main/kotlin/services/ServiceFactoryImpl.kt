package com.profgroep8.services

import com.profgroep8.Util.RdwClient
import com.profgroep8.interfaces.repositories.DatabaseFactory
import com.profgroep8.interfaces.services.CarService
import com.profgroep8.interfaces.services.RentalLocationService
import com.profgroep8.interfaces.services.RentalService
import com.profgroep8.interfaces.services.ServiceFactory
import com.profgroep8.interfaces.services.UserService
import io.ktor.server.config.ApplicationConfig

open class ServiceFactoryImpl(
    val databaseFactory: DatabaseFactory,
    config: ApplicationConfig,
) : ServiceFactory {

    init {
        RdwClient.init(config) // initialize once
    }

    override val rdwClient: RdwClient = RdwClient
    override val carService: CarService = CarServiceImpl(this)
    override val userService: UserService = UserServiceImpl(this)
    override val rentalService: RentalService = RentalServiceImpl(this)
    override val rentalLocationService: RentalLocationService = RentalLocationServiceImpl(this)
}
