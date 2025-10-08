package com.profgroep8.Controller.User

import com.profgroep8.interfaces.services.ServiceFactory
import com.profgroep8.models.dto.CreateUserDTO
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.userRoutes(serviceFactory: ServiceFactory) {

    routing {
        route("/users") {

            post("/register") {
                val createUserDTO = try {
                    call.receive<CreateUserDTO>()
                } catch (e: Exception) {
                    throw BadRequestException("Invalid request body")
                }

                try {
                    // Notice: lowercase 'userService'
                    val newUser = serviceFactory.userService.register(createUserDTO)
                    call.respond(newUser)
                } catch (e: BadRequestException) {
                    call.respondText(e.message ?: "Registration failed")
                } catch (e: Exception) {
                    call.respondText("Unexpected error during registration")
                }
            }

        }
    }
}