package com.profgroep8.integrationtests

import com.profgroep8.integrations.BaseIntegrationTest
import com.profgroep8.models.dto.CreateRentalDTO
import com.profgroep8.models.dto.CreateRentalLocationDTO
import com.profgroep8.models.dto.CreateCarDTO
import com.profgroep8.models.dto.CarDTO
import com.profgroep8.models.dto.RentalDTO
import com.profgroep8.models.dto.RentalWithLocationsDTO
import com.profgroep8.models.dto.UpdateRentalDTO
import com.profgroep8.models.dto.UpdateRentalLocationDTO
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.*

class RentalIntegrationTest : BaseIntegrationTest() {

    @Test
    fun testGetAllRentals() = runTest {
        val token = generateToken()
        val response = get("/rentals") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertNotNull(responseBody)
        // Should return an array (empty or with data)
        assertTrue(responseBody.startsWith("[") && responseBody.endsWith("]"))
    }

    @Test
    fun testGetRentalByIdNotFound() = runTest {
        val token = generateToken()
        val response = get("/rentals/99999") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testGetRentalByIdInvalidId() = runTest {
        val token = generateToken()
        val response = get("/rentals/invalid") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun testCreateRentalInvalidData() = runTest {
        val token = generateToken()
        val invalidRentalDTO = CreateRentalDTO(
            carID = -1,  // Invalid car ID
            startLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-01T10:00:00"),
                longitude = 0f, // Invalid coordinates
                latitude = 0f
            ),
            endLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-01T18:00:00"),
                longitude = 0f,
                latitude = 0f
            )
        )

        val response = post("/rentals") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(invalidRentalDTO))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun testCreateRentalWithInvalidJson() = runTest {
        val token = generateToken()
        val response = post("/rentals") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("invalid json")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun testCreateRentalHappyPath() = runTest {
        val token = generateToken()

        // Create a car first
        val carResponse = post("/cars") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CreateCarDTO.serializer(),
                    CreateCarDTO(
                        licensePlate = "TEST-${(1000..9999).random()}",
                        brand = "BMW",
                        model = "X5",
                        year = 2025,
                        fuelType = 1,
                        price = 85000
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.Created, carResponse.status)
        val car = Json.decodeFromString(CarDTO.serializer(), carResponse.bodyAsText())

        // Now test creating a rental with valid references
        val validRentalDTO = CreateRentalDTO(
            carID = car.carID,
            startLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-02T10:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            ),
            endLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-02T18:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            )
        )

        val response = post("/rentals") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(validRentalDTO))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertNotNull(responseBody)
    }

    @Test
    fun testCreateRentalWrongBody() = runTest {
        val token = generateToken()
        val response = post("/rentals") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"invalid": "json", "missing": "required", "fields": true}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun testUpdateRentalNotFound() = runTest {
        val token = generateToken()
        val updateRentalDTO = UpdateRentalDTO(state = 0)

        val response = put("/rentals/99999") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(updateRentalDTO))
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testUpdateRentalHappyPath() = runTest {
        val token = generateToken()

        // Create a car first
        val carResponse = post("/cars") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CreateCarDTO.serializer(),
                    CreateCarDTO(
                        licensePlate = "TEST-${(1000..9999).random()}",
                        brand = "Tesla",
                        model = "Model S",
                        year = 2025,
                        fuelType = 1,
                        price = 75000
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.Created, carResponse.status)
        val car = Json.decodeFromString(CarDTO.serializer(), carResponse.bodyAsText())

        // Create a rental
        val createRentalDTO = CreateRentalDTO(
            carID = car.carID,
            startLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-01T10:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            ),
            endLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-01T18:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            )
        )

        val createResponse = post("/rentals") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(createRentalDTO))
        }

        assertEquals(HttpStatusCode.OK, createResponse.status)
        val rental = Json.decodeFromString(RentalWithLocationsDTO.serializer(), createResponse.bodyAsText())

        // Now test updating the rental
        val updateRentalDTO = UpdateRentalDTO(state = 0)

        val response = put("/rentals/${rental.rentalID}") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(updateRentalDTO))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertNotNull(responseBody)
    }

    @Test
    fun testUpdateRentalWrongBody() = runTest {
        val token = generateToken()
        
        // Create a car first
        val carResponse = post("/cars") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CreateCarDTO.serializer(),
                    CreateCarDTO(
                        licensePlate = "TEST-${(1000..9999).random()}",
                        brand = "BMW",
                        model = "X5",
                        year = 2025,
                        fuelType = 1,
                        price = 85000
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.Created, carResponse.status)
        val car = Json.decodeFromString(CarDTO.serializer(), carResponse.bodyAsText())

        // Create a rental
        val createRentalDTO = CreateRentalDTO(
            carID = car.carID,
            startLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-01T10:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            ),
            endLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-01T18:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            )
        )

        val createResponse = post("/rentals") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(createRentalDTO))
        }

        assertEquals(HttpStatusCode.OK, createResponse.status)
        val rental = Json.decodeFromString(RentalWithLocationsDTO.serializer(), createResponse.bodyAsText())

        // Now test updating with wrong body
        val response = put("/rentals/${rental.rentalID}") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"invalid": "json", "missing": "state", "field": true}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun testEndRentalNotFound() = runTest {
        val token = generateToken()
        val response = put("/rentals/99999/end") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testEndRentalHappyPath() = runTest {
        val token = generateToken()

        // Create a car first
        val carResponse = post("/cars") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CreateCarDTO.serializer(),
                    CreateCarDTO(
                        licensePlate = "TEST-${(1000..9999).random()}",
                        brand = "Tesla",
                        model = "Model S",
                        year = 2025,
                        fuelType = 1,
                        price = 75000
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.Created, carResponse.status)
        val car = Json.decodeFromString(CarDTO.serializer(), carResponse.bodyAsText())

        // Create a rental
        val createRentalDTO = CreateRentalDTO(
            carID = car.carID,
            startLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-01T10:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            ),
            endLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-01T18:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            )
        )

        val createResponse = post("/rentals") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(createRentalDTO))
        }

        assertEquals(HttpStatusCode.OK, createResponse.status)
        val rental = Json.decodeFromString(RentalWithLocationsDTO.serializer(), createResponse.bodyAsText())

        val response = put("/rentals/${rental.rentalID}/end") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertNotNull(responseBody)
    }

    @Test
    fun testGetRentalLocationsNotFound() = runTest {
        val token = generateToken()
        val response = get("/rentals/99999/locations") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testGetRentalLocationsHappyPath() = runTest {
        val token = generateToken()

        // Create a car first
        val carResponse = post("/cars") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CreateCarDTO.serializer(),
                    CreateCarDTO(
                        licensePlate = "TEST-${(1000..9999).random()}",
                        brand = "Tesla",
                        model = "Model S",
                        year = 2025,
                        fuelType = 1,
                        price = 75000
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.Created, carResponse.status)
        val car = Json.decodeFromString(CarDTO.serializer(), carResponse.bodyAsText())

        // Create a rental
        val createRentalDTO = CreateRentalDTO(
            carID = car.carID,
            startLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-01T10:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            ),
            endLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-01T18:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            )
        )

        val createResponse = post("/rentals") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(createRentalDTO))
        }

        assertEquals(HttpStatusCode.OK, createResponse.status)
        val rental = Json.decodeFromString(RentalWithLocationsDTO.serializer(), createResponse.bodyAsText())

        val response = get("/rentals/${rental.rentalID}/locations") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertNotNull(responseBody)
    }

    @Test
    fun testUpdateRentalLocationNotFound() = runTest {
        val token = generateToken()
        val updateLocationDTO = UpdateRentalLocationDTO(
            date = LocalDateTime.parse("2025-01-01T12:00:00"),
            longitude = 4.9041f,
            latitude = 52.3676f
        )

        val response = put("/rentals/99999/locations/99999") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(updateLocationDTO))
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testUpdateRentalLocationHappyPath() = runTest {
        val token = generateToken()

        // Create a car first
        val carResponse = post("/cars") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CreateCarDTO.serializer(),
                    CreateCarDTO(
                        licensePlate = "TEST-${(1000..9999).random()}",
                        brand = "Tesla",
                        model = "Model S",
                        year = 2025,
                        fuelType = 1,
                        price = 75000
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.Created, carResponse.status)
        val car = Json.decodeFromString(CarDTO.serializer(), carResponse.bodyAsText())

        // Create a rental
        val createRentalDTO = CreateRentalDTO(
            carID = car.carID,
            startLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-01T10:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            ),
            endLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-01T18:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            )
        )

        val createResponse = post("/rentals") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(createRentalDTO))
        }

        assertEquals(HttpStatusCode.OK, createResponse.status)
        val rental = Json.decodeFromString(RentalWithLocationsDTO.serializer(), createResponse.bodyAsText())

        val updateLocationDTO = UpdateRentalLocationDTO(
            date = LocalDateTime.parse("2025-01-01T12:00:00"),
            longitude = 4.9041f,
            latitude = 52.3676f
        )

        val response = put("/rentals/${rental.rentalID}/locations/${rental.startRentalLocation.rentalLocationID}") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(updateLocationDTO))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertNotNull(responseBody)
    }

    @Test
    fun testUpdateRentalLocationWrongBody() = runTest {
        val token = generateToken()
        val response = put("/rentals/1/locations/1") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("invalid json")
        }

        // Should return 404 since rental with ID 1 doesn't exist in test database
        // The rental check happens before JSON parsing
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testDeleteRentalNotFound() = runTest {
        val token = generateToken()
        val response = delete("/rentals/99999") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testDeleteRentalHappyPath() = runTest {
        val token = generateToken()

        // Create a car first
        val carResponse = post("/cars") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CreateCarDTO.serializer(),
                    CreateCarDTO(
                        licensePlate = "TEST-${(1000..9999).random()}",
                        brand = "Tesla",
                        model = "Model S",
                        year = 2025,
                        fuelType = 1,
                        price = 75000
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.Created, carResponse.status)
        val car = Json.decodeFromString(CarDTO.serializer(), carResponse.bodyAsText())

        // Create a rental
        val createRentalDTO = CreateRentalDTO(
            carID = car.carID,
            startLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-01T10:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            ),
            endLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-01T18:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            )
        )

        val createResponse = post("/rentals") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(createRentalDTO))
        }

        assertEquals(HttpStatusCode.OK, createResponse.status)
        val rental = Json.decodeFromString(RentalWithLocationsDTO.serializer(), createResponse.bodyAsText())

        val response = delete("/rentals/${rental.rentalID}") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertNotNull(responseBody)
    }

    @Test
    fun testGetRentalByIdHappyPath() = runTest {
        val token = generateToken()

        // Create a car first
        val carResponse = post("/cars") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CreateCarDTO.serializer(),
                    CreateCarDTO(
                        licensePlate = "TEST-${(1000..9999).random()}",
                        brand = "BMW",
                        model = "X3",
                        year = 2024,
                        fuelType = 0,
                        price = 50000
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.Created, carResponse.status)
        val car = Json.decodeFromString(CarDTO.serializer(), carResponse.bodyAsText())

        // Create a rental
        val createRentalDTO = CreateRentalDTO(
            carID = car.carID,
            startLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-01T10:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            ),
            endLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2025-01-01T18:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            )
        )

        val createResponse = post("/rentals") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(createRentalDTO))
        }

        assertEquals(HttpStatusCode.OK, createResponse.status)
        val rental = Json.decodeFromString(RentalWithLocationsDTO.serializer(), createResponse.bodyAsText())

        // Get the rental by ID
        val response = get("/rentals/${rental.rentalID}") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertNotNull(responseBody)
        assertTrue(responseBody.contains(rental.rentalID.toString()))
    }

}
