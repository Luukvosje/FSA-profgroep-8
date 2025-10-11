package com.profgroep8.plugins

//import com.profgroep8.Controller.Car.carRoutes
import com.profgroep8.Controller.Car.carRoutes
import com.profgroep8.Controller.User.userRoutes
import com.profgroep8.interfaces.services.ServiceFactory
import com.profgroep8.services.ServiceFactoryImpl
import io.ktor.server.application.*

fun Application.configureRouting(serviceFactory: ServiceFactory) {
    carRoutes(  serviceFactory)
    userRoutes(serviceFactory)
}
