package com.example.network.services

import com.example.network.interfaces.services.CarService
import com.example.network.interfaces.services.RentalService
import com.example.network.interfaces.services.ServiceFactory
import com.example.network.interfaces.services.UserService
import com.example.network.models.domain.User

class ServiceFactoryImpl : ServiceFactory {
    override val carService: CarService by lazy { CarServiceImpl() }
    override val rentalService: RentalService by lazy { RentalServiceImpl() }
    override val userService: UserService by lazy { UserServiceImpl() }
    private val baseServices = mutableListOf<BaseServiceImpl>()

    override fun setToken(token: String?) {
        TokenProvider.token = token
    }

    override fun setUser(user: User) {
        UserProvider.user = user;
    }
}

object TokenProvider {
    var token: String? = null
}

object UserProvider {
    var user: User? = null
}
