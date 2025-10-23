package com.profgroep8.Controller.User

import com.profgroep8.exceptions.UnauthorizedException
import com.profgroep8.interfaces.services.ServiceFactory
import com.profgroep8.models.dto.CreateUserDTO
import com.profgroep8.models.dto.LoginUserDTO
import getUserContext
import requireUserContext
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*

fun Application.userRoutes(serviceFactory: ServiceFactory) {

    routing {
        route("/users") {

            // CREATE / REGISTER USER
            post("/register") {
                val createUserDTO = call.receive<CreateUserDTO>()

                // Check if user with this email already exists
                val existingUser = serviceFactory.userService.getByEmail(createUserDTO.email)
                if (existingUser != null) {
                    throw BadRequestException("User already exists with this email")
                }

                val newUser = serviceFactory.userService.register(createUserDTO)
                call.respond(HttpStatusCode.Created, newUser)
            }

            // LOGIN USER
            post("/login") {
                val loginUserDTO = call.receive<LoginUserDTO>()
                val loginResponse = serviceFactory.userService.login(loginUserDTO)
                call.respond(HttpStatusCode.OK, loginResponse)
            }

            // AUTHENTICATED ROUTES
            authenticate("jwt") {

                // GET LOGGED-IN USER (/users/me)
                get("/me") {
                    val user = call.getUserContext()
                    val email = user?.email ?: throw BadRequestException("User not found")
                    val userDTO = serviceFactory.userService.getByEmail(email)
                        ?: throw BadRequestException("User not found")
                    call.respond(userDTO)
                }

                // GET BONUS POINTS (/users/{id}/bonuspoints)
                get("/{id}/bonuspoints") {
                    val user = call.requireUserContext()
                    val userId = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid user ID")

                    // Only allow the user to view their own points
                    if (user.userID != userId) throw UnauthorizedException()

                    val points = serviceFactory.userService.getBonusPoints(userId)
                    call.respond(HttpStatusCode.OK, mapOf("bonusPoints" to points))
                }

                // UPDATE BONUS POINTS (/users/{id}/bonuspoints)
                put("/{id}/bonuspoints") {
                    val user = call.requireUserContext()
                    val userId = call.parameters["id"]?.toIntOrNull()
                        ?: throw BadRequestException("Invalid user ID")

                    // Only allow updating own points
                    if (user.userID != userId) throw UnauthorizedException()

                    val body = call.receive<Map<String, Int>>()
                    val points = body["points"] ?: throw BadRequestException("Missing points field")

                    val updatedUser = serviceFactory.userService.updateBonusPoints(userId, points)
                    call.respond(HttpStatusCode.OK, updatedUser)
                }
            }
        }
    }
}