package com.profgroep8.exceptions

import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.http.*
import io.ktor.server.response.*
import com.profgroep8.exceptions.*

class NotFoundException : Exception() // 404 NOT_FOUND
class ConflictException : Exception() // 409 CONFLICT

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<NotFoundException> { call, cause ->
            call.respond(
                status = HttpStatusCode.NotFound,
                message = mapOf(
                    "error" to "Resource not found",
                    "details" to (cause.message ?: "The requested resource could not be found.")
                )
            )
        }

        exception<ConflictException> { call, cause ->
            call.respond(
                status = HttpStatusCode.Conflict,
                message = mapOf(
                    "error" to "Conflict detected",
                    "details" to (cause.message ?: "The request could not be completed due to a conflict.")
                )
            )
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