package com.profgroep8.Controller.User

import com.profgroep8.interfaces.services.ServiceFactory
import com.profgroep8.models.dto.CreateUserDTO
import com.profgroep8.models.dto.LoginUserDTO
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.response.*

fun Application.userRoutes(serviceFactory: ServiceFactory) {

    routing {
        route("/users") {

            post("/register") {
                val createUserDTO = call.receive<CreateUserDTO>()
                val newUser = serviceFactory.userService.register(createUserDTO)
                call.respond(newUser)
            }

            post("/login") {
                val loginUserDTO = call.receive<LoginUserDTO>()
                val user = serviceFactory.userService.login(loginUserDTO)
                call.respond(user)
            }

            // New route: check if logged in
            authenticate("jwt") {
                get("/me") {
                    val principal = call.principal<JWTPrincipal>()
                    val email = principal!!.payload.getClaim("email").asString()

                    val user = serviceFactory.userService.getByEmail(email)
                    if (user == null) {
                        call.respondText("User not found", status = io.ktor.http.HttpStatusCode.NotFound)
                    } else {
                        call.respond(user)
                    }
                }
            }

        }
    }
}