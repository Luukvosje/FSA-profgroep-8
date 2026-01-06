package com.example.network.services

import com.example.network.interfaces.services.CarService
import com.example.network.interfaces.services.RentalService
import com.example.network.interfaces.services.ServiceFactory

class ServiceFactoryImpl : ServiceFactory {
    override val carService: CarService by lazy { CarServiceImpl() }
    override val rentalService: RentalService by lazy { RentalServiceImpl() }
    private val baseServices = mutableListOf<BaseServiceImpl>()

    override fun setToken(token: String?) {
        TokenProvider.token = token
    }

}
object TokenProvider {
    var token: String? = null
}
