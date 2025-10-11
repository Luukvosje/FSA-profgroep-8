package com.profgroep8.services

import com.profgroep8.Util.RdwImpl
import com.profgroep8.interfaces.repositories.DatabaseFactory
import com.profgroep8.interfaces.services.CarService
import com.profgroep8.interfaces.services.ServiceFactory
import com.profgroep8.interfaces.services.UserService
import io.ktor.server.config.ApplicationConfig

open class ServiceFactoryImpl(
    val databaseFactory: DatabaseFactory,
    config: ApplicationConfig,
) : ServiceFactory {
    override val rdwService: RdwImpl = RdwImpl(config)

    override val carService: CarService = CarServiceImpl(this)
    override val userService: UserService = UserServiceImpl()
}
