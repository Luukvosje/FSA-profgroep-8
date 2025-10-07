package com.profgroep8.services

import com.profgroep8.interfaces.repositories.DatabaseFactory
import com.profgroep8.interfaces.services.CarService
import com.profgroep8.interfaces.services.ServiceFactory

open class ServiceFactoryImpl(
    val databaseFactory: DatabaseFactory
) : ServiceFactory {

    override val carService: CarService = CarServiceImpl(this)
}
