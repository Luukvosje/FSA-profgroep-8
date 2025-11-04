package com.profgroep8.integrationtests

import com.profgroep8.integrations.BaseIntegrationTest
import com.profgroep8.models.dto.CreateCarDTO
import com.profgroep8.models.dto.UpdateCarDTO
import com.profgroep8.models.dto.CarDTO
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.*

class CarIntegrationTest : BaseIntegrationTest() {
    @Test
    fun testCreateCar() = runTest {
        val token = generateToken()

        val newCar = CreateCarDTO(
            licensePlate = "AB-123-CD",
            brand = "Toyota",
            model = "Corolla",
            price = 0,
            year = 2020,
            fuelType = 0
        )

        val response = post("/cars") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(CreateCarDTO.serializer(), newCar))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val createdCar = Json.decodeFromString(CarDTO.serializer(), response.bodyAsText())

        assertNotNull(createdCar)
        assertEquals("toyota", createdCar.brand.lowercase())
        assertEquals("corolla", createdCar.model.lowercase())
    }

    @Test
    fun testCreateCar_Unauthorized_NoToken() = runTest {
        val newCar = CreateCarDTO(
            licensePlate = "NO-000-TK",
            brand = "Honda",
            model = "Civic",
            price = 12000,
            year = 2018,
            fuelType = 0
        )

        val response = post("/cars") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(CreateCarDTO.serializer(), newCar))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun testGetAllCars() = runTest {
        val token = generateToken()

        // Pre-create one car
        val createResponse = post("/cars") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CreateCarDTO.serializer(),
                    CreateCarDTO(
                        licensePlate = "AA-001-BB",
                        brand = "Tesla",
                        model = "Model 3",
                        price = 100000,
                        year = 2021,
                        fuelType = 1
                    )
                )
            )
        }
        assertEquals(HttpStatusCode.Created, createResponse.status)

        // Get all
        val response = get("/cars") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val cars = Json.decodeFromString(ListSerializer(CarDTO.serializer()), response.bodyAsText())

        assertTrue(cars.isNotEmpty())
        assertTrue(cars.any { it.brand.equals("Tesla", ignoreCase = true) })
    }

    @Test
    fun testGetAllCars_Unauthorized() = runTest {
        val response = get("/cars")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun testGetSingleCar_NotFound() = runTest {
        val token = generateToken()

        val response = get("/cars/9999") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testUpdateCar() = runTest {
        val token = generateToken()

        // Create a car first
        val createResponse = post("/cars") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CreateCarDTO.serializer(),
                    CreateCarDTO(
                        licensePlate = "XX-999-ZZ",
                        brand = "BMW",
                        model = "320i",
                        price = 10000,
                        year = 2022,
                        fuelType = 0
                    )
                )
            )
        }

        val createdCar = Json.decodeFromString(CarDTO.serializer(), createResponse.bodyAsText())
        assertNotNull(createdCar)

        // Update it
        val updatedCarDTO = UpdateCarDTO(
            licensePlate = "YY-111-ZZ",
            brand = "BMW",
            model = "330i",
            price = 33000,
            year = 2023,
            fuelType = 3
        )

        val updateResponse = put("/cars/${createdCar.carID}") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(UpdateCarDTO.serializer(), updatedCarDTO))
        }

        assertEquals(HttpStatusCode.OK, updateResponse.status)
        val updatedCar = Json.decodeFromString(CarDTO.serializer(), updateResponse.bodyAsText())
        assertEquals("330i", updatedCar.model)
        assertEquals(3, updatedCar.fuelType)
    }

    @Test
    fun testUpdateCar_NotFound() = runTest {
        val token = generateToken()

        val updateDTO = UpdateCarDTO(
            licensePlate = "AA-000-BB",
            brand = "Fake",
            model = "Ghost",
            price = 15000,
            year = 2000,
            fuelType = 0
        )

        val response = put("/cars/9999") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(UpdateCarDTO.serializer(), updateDTO))
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testUpdateCar_Unauthorized() = runTest {
        val updateDTO = UpdateCarDTO(
            licensePlate = "ZZ-000-AA",
            brand = "Fake",
            model = "Ghost",
            price = 1300,
            year = 2000,
            fuelType = 0
        )

        val response = put("/cars/1") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(UpdateCarDTO.serializer(), updateDTO))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    // --------------------------------
    // DELETE
    // --------------------------------

    @Test
    fun testDeleteCar() = runTest {
        val token = generateToken()

        // Create a car first
        val createResponse = post("/cars") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CreateCarDTO.serializer(),
                    CreateCarDTO(
                        licensePlate = "DEL-123",
                        brand = "Ford",
                        model = "Focus",
                        price = 12000,
                        year = 2019,
                        fuelType = 2
                    )
                )
            )
        }

        val createdCar = Json.decodeFromString(CarDTO.serializer(), createResponse.bodyAsText())
        assertNotNull(createdCar)

        // Delete it
        val deleteResponse = delete("/cars/${createdCar.carID}") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, deleteResponse.status)

        // Verify it's gone
        val getResponse = get("/cars/${createdCar.carID}") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }

    @Test
    fun testDeleteCar_NotFound() = runTest {
        val token = generateToken()

        val response = delete("/cars/9999") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testDeleteCar_Unauthorized() = runTest {
        val response = delete("/cars/1")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
