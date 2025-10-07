package com.profgroep8

import com.profgroep8.models.dto.CreateCarDTO
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testCreateCar() = testApplication {
        application {
            module()
        }

        // Prepare a sample DTO
        val carDTO = CreateCarDTO(
            licensePlate = "TEST-123",
            brand = "Tesla",
            model = "Model S",
            year = 2025,
            fuelType = 1,
            price = 75000
        )

        // Send POST request
        val response: HttpResponse = client.post("/cars") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(carDTO))
        }

        // Check response
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertNotNull(responseBody)
        // Optionally, you can deserialize to CarDTO and check fields
    }
}
