package com.profgroep8.exceptions

import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.http.*
import io.ktor.server.response.*
import com.profgroep8.exceptions.*
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.BadRequestException

class ConflictException : Exception() // 409 CONFLICT

fun Application.configureStatusPages() {
    install(StatusPages) {
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