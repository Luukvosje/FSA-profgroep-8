package com.profgroep8.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureOpenAPI() {
    routing {
        // Serve OpenAPI specification
        get("/openapi/documentation.yaml") {
            val yamlContent = this::class.java.classLoader.getResource("openapi/documentation.yaml")?.readText()
                ?: throw io.ktor.server.plugins.NotFoundException("OpenAPI documentation not found")
            call.respondText(yamlContent, ContentType.Application.Yaml)
        }
        
        // Swagger UI
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml") {
            version = "4.15.5"
        }
    }
}
