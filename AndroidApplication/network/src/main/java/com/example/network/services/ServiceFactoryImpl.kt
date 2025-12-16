package com.example.network.services

import com.example.network.interfaces.services.CarService
import com.example.network.interfaces.services.ServiceFactory

class ServiceFactoryImpl : ServiceFactory {
    override val carService: CarService by lazy { CarServiceImpl() }
}