package com.profgroep8.exceptions

import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.BadRequestException
import org.jetbrains.exposed.exceptions.ExposedSQLException

class ConflictException(message: String) : Exception(message) // 409 CONFLICT
class UnauthorizedException : Exception() //401 UNAUTHORIZED

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<UnauthorizedException> { call, _ ->
            call.respond(
                HttpStatusCode.Unauthorized,
                mapOf(
                    "error" to "Unauthorized",
                    "details" to "Missing or invalid authentication token."
                )
            )
        }

        exception<BadRequestException> { call, cause ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = mapOf(
                    "error" to "Bad Request",
                    "details" to (cause.message ?: "The request could not be processed.")
                )
            )
        }

        exception<ConflictException> { call, cause ->
            call.respond(HttpStatusCode.Conflict,
                message = mapOf(
                    "error" to "Conflict detected",
                    "details" to (cause.message ?: "The request could not be completed due to a conflict.")
                )
            )
        }

        exception<NotFoundException> { call, cause ->
            call.respond(
                status = HttpStatusCode.NotFound,
                message = mapOf(
                    "error" to "Not Found",
                    "details" to (cause.message ?: "The requested object was not found.")
                )
            )
        }

        exception<ExposedSQLException> { call, cause ->
            val message = cause.message ?: "Database error occurred"
            when {
                message.contains("violates foreign key constraint") -> {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = mapOf(
                            "error" to "Bad Request",
                            "details" to "Invalid reference: One or more referenced entities do not exist."
                        )
                    )
                }
                message.contains("duplicate key") -> {
                    call.respond(
                        status = HttpStatusCode.Conflict,
                        message = mapOf(
                            "error" to "Conflict",
                            "details" to "Resource already exists."
                        )
                    )
                }
                else -> {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = mapOf(
                            "error" to "Bad Request",
                            "details" to "Invalid data provided."
                        )
                    )
                }
            }
        }

        exception<Throwable> { call, cause ->
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = mapOf(
                    "error" to "Internal server error",
                    "details" to (cause.message ?: "An unexpected error occurred.")
                )
            )
        }
    }
}