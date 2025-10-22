package com.profgroep8

import com.profgroep8.models.dto.CreateCarDTO
import com.profgroep8.models.dto.CreateRentalDTO
import com.profgroep8.models.dto.CreateRentalLocationDTO
import com.profgroep8.models.dto.CreateUserDTO
import com.profgroep8.models.dto.LoginUserDTO
import com.profgroep8.models.dto.UpdateRentalDTO
import com.profgroep8.models.dto.UpdateRentalLocationDTO
import com.profgroep8.plugins.JwtConfig
import com.profgroep8.services.ServiceFactoryImpl
import com.profgroep8.repositories.DatabaseFactoryImpl
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.*
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import java.util.UUID

class ApplicationTest {
    
    // Setup function to create test data using services directly
    private fun setupTestData(): Triple<Int, Int, Int> {
        val testConfig = MapApplicationConfig(
            "ktor.jwt.secret" to "testsecret",
            "ktor.jwt.issuer" to "testissuer",
            "ktor.jwt.audience" to "testaudience",
            "ktor.jwt.realm" to "testrealm",
            "ktor.rdw.apiKey" to "testapikey"
        )
        DatabaseFactoryImpl.init()
        val serviceFactory = ServiceFactoryImpl(DatabaseFactoryImpl, testConfig)
        
        // Generate unique identifiers to avoid conflicts
        val uniqueId = (1000..9999).random()
        
        // Create test user
        val userDTO = CreateUserDTO(
            fullName = "Test User $uniqueId",
            email = "test$uniqueId@example.com",
            password = "testpassword123",
            phone = "+31687654321",
            address = "123 Test Street",
            zipcode = "1000AB",
            city = "Amsterdam",
            countryISO = "NL"
        )
        val createdUser = serviceFactory.userService.register(userDTO)
        
        // Create test car
        val carDTO = CreateCarDTO(
            licensePlate = "TEST-$uniqueId",
            brand = "Tesla",
            model = "Model S",
            year = 2025,
            fuelType = 1,
            price = 75000
        )
        val createdCar = serviceFactory.carService.create(carDTO, 0)
        
        // Create test rental
        val createRentalDTO = CreateRentalDTO(
            carID = createdCar?.carID ?: 0,
            startLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2024-01-01T10:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            ),
            endLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2024-01-01T18:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            )
        )
        val createdRental = serviceFactory.rentalService.create(createRentalDTO, 0)
        
        return Triple(createdUser.userID, createdCar?.carID ?: 0, createdRental.rentalID)
    }

    private fun initJwtForTest() {
        val testConfig = MapApplicationConfig(
            "ktor.jwt.secret" to "testsecret",
            "ktor.jwt.issuer" to "testissuer",
            "ktor.jwt.audience" to "testaudience",
            "ktor.jwt.realm" to "testrealm",
            "ktor.rdw.apiKey" to "testapikey"
        )
        JwtConfig.init(testConfig)
    }

    @Test
    fun testRoot() = testApplication {
        initJwtForTest()
        application {
            module()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testCreateCar() = testApplication {
        initJwtForTest()
        application { module() }

        val carDTO = CreateCarDTO(
            licensePlate = "TEST-123",
            brand = "Tesla",
            model = "Model S",
            year = 2025,
            fuelType = 1,
            price = 75000
        )

        val response: HttpResponse = client.post("/cars") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(carDTO))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertNotNull(responseBody)
    }

    @Test
    fun testRegisterUser() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.secret" to "testsecret",
                "ktor.jwt.issuer" to "testissuer",
                "ktor.jwt.audience" to "testaudience",
                "ktor.jwt.realm" to "testrealm",
                "ktor.rdw.apiKey" to "testapikey"
            )
        }
        application { module() }

        val randomEmail = "paul_" + UUID.randomUUID().toString() + "@example.com"
        val createUserDTO = CreateUserDTO(
            fullName = "Paul De Mast",
            email = randomEmail,
            password = "test123",
            phone = "+31687654321",
            address = "456 Street",
            zipcode = "2000CD",
            city = "Rotterdam",
            countryISO = "NL"
        )

        val response: HttpResponse = client.post("/users/register") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(createUserDTO))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertNotNull(responseBody)
        assert(responseBody.contains("Paul De Mast"))
        assert(responseBody.contains(randomEmail))
    }

    @Test
    fun testLoginUser() = testApplication {
        environment { config = MapApplicationConfig(
            "ktor.jwt.secret" to "testsecret",
            "ktor.jwt.issuer" to "testissuer",
            "ktor.jwt.audience" to "testaudience",
            "ktor.jwt.realm" to "testrealm",
            "ktor.rdw.apiKey" to "testapikey"
        )}

        application { module() }

        val email = "pauldm@example.com"
        val createUserDTO = CreateUserDTO(
            fullName = "Paul De Mast",
            email = email,
            password = "test123",
            phone = "+31687654321",
            address = "456 Street",
            zipcode = "2000CD",
            city = "Rotterdam",
            countryISO = "NL"
        )

        client.post("/users/register") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(createUserDTO))
        }

        val loginUserDTO = LoginUserDTO(email, "test123")
        val response: HttpResponse = client.post("/users/login") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(loginUserDTO))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertNotNull(responseBody)
        assert(responseBody.contains("token"))
    }

    @Test
    fun testGetLoggedInUser() = testApplication {
        environment { config = MapApplicationConfig(
            "ktor.jwt.secret" to "testsecret",
            "ktor.jwt.issuer" to "testissuer",
            "ktor.jwt.audience" to "testaudience",
            "ktor.jwt.realm" to "testrealm",
            "ktor.rdw.apiKey" to "testapikey"
        )}

        application { module() }

        val email = "meuser_${UUID.randomUUID()}@example.com"
        val createUserDTO = CreateUserDTO(
            fullName = "Me User",
            email = email,
            password = "test123",
            phone = "+31600000000",
            address = "Test Street",
            zipcode = "1234AB",
            city = "TestCity",
            countryISO = "NL"
        )

        // register
        client.post("/users/register") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(createUserDTO))
        }

        // login
        val loginResponse: HttpResponse = client.post("/users/login") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(LoginUserDTO(email, "test123")))
        }

        val loginBody = loginResponse.bodyAsText()
        val token = Regex("\"token\":\"([^\"]+)\"").find(loginBody)!!.groupValues[1]

        // get logged in user
        val response: HttpResponse = client.get("/users/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertNotNull(responseBody)
        assert(responseBody.contains(email))
    }

    @Test
    fun testGetBonusPoints() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.secret" to "testsecret",
                "ktor.jwt.issuer" to "testissuer",
                "ktor.jwt.audience" to "testaudience",
                "ktor.jwt.realm" to "testrealm",
                "ktor.rdw.apiKey" to "testapikey"
            )
        }
        JwtConfig.init(
            MapApplicationConfig(
                "ktor.jwt.secret" to "testsecret",
                "ktor.jwt.issuer" to "testissuer",
                "ktor.jwt.audience" to "testaudience",
                "ktor.jwt.realm" to "testrealm",
                "ktor.rdw.apiKey" to "testapikey"
            )
        )
        application { module() }
        val token = JwtConfig.generateToken("1", "pauldemast@example.com")
        val response: HttpResponse = client.get("/users/1/bonuspoints") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertNotNull(responseBody)
        assert(responseBody.contains("bonusPoints"))
    }

    @Test
    fun testUpdateBonusPoints() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.secret" to "testsecret",
                "ktor.jwt.issuer" to "testissuer",
                "ktor.jwt.audience" to "testaudience",
                "ktor.jwt.realm" to "testrealm",
                "ktor.rdw.apiKey" to "testapikey"
            )
        }
        JwtConfig.init(
            MapApplicationConfig(
                "ktor.jwt.secret" to "testsecret",
                "ktor.jwt.issuer" to "testissuer",
                "ktor.jwt.audience" to "testaudience",
                "ktor.jwt.realm" to "testrealm",
                "ktor.rdw.apiKey" to "testapikey"
            )
        )
        application { module() }
        val token = JwtConfig.generateToken("1", "pauldemast@example.com")
        val response: HttpResponse = client.put("/users/1/bonuspoints") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{ "points": 200 }""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertNotNull(responseBody)
        assert(responseBody.contains("200"))
    }

    @Test
    fun testGetAllRentals() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.secret" to "testsecret",
                "ktor.jwt.issuer" to "testissuer",
                "ktor.jwt.audience" to "testaudience",
                "ktor.jwt.realm" to "testrealm",
                "ktor.rdw.apiKey" to "testapikey"
            )
        }
        initJwtForTest()
        application { module() }

        val response: HttpResponse = client.get("/rentals")

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertNotNull(responseBody)
        // Should return an array (empty or with data)
        assertTrue(responseBody.startsWith("[") && responseBody.endsWith("]"))
    }

    @Test
    fun testGetRentalByIdNotFound() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.secret" to "testsecret",
                "ktor.jwt.issuer" to "testissuer",
                "ktor.jwt.audience" to "testaudience",
                "ktor.jwt.realm" to "testrealm",
                "ktor.rdw.apiKey" to "testapikey"
            )
        }
        initJwtForTest()
        application { module() }

        val response: HttpResponse = client.get("/rentals/99999")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testGetRentalByIdInvalidId() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.secret" to "testsecret",
                "ktor.jwt.issuer" to "testissuer",
                "ktor.jwt.audience" to "testaudience",
                "ktor.jwt.realm" to "testrealm",
                "ktor.rdw.apiKey" to "testapikey"
            )
        }
        initJwtForTest()
        application { module() }

        val response: HttpResponse = client.get("/rentals/invalid")

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun testCreateRentalInvalidData() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.secret" to "testsecret",
                "ktor.jwt.issuer" to "testissuer",
                "ktor.jwt.audience" to "testaudience",
                "ktor.jwt.realm" to "testrealm",
                "ktor.rdw.apiKey" to "testapikey"
            )
        }
        initJwtForTest()
        application { module() }

        val invalidRentalDTO = CreateRentalDTO(
            carID = -1,  // Invalid car ID
            startLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2024-01-01T10:00:00"),
                longitude = 0f, // Invalid coordinates
                latitude = 0f
            ),
            endLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2024-01-01T18:00:00"),
                longitude = 0f,
                latitude = 0f
            )
        )

        val response: HttpResponse = client.post("/rentals") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(invalidRentalDTO))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun testUpdateRentalNotFound() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.secret" to "testsecret",
                "ktor.jwt.issuer" to "testissuer",
                "ktor.jwt.audience" to "testaudience",
                "ktor.jwt.realm" to "testrealm",
                "ktor.rdw.apiKey" to "testapikey"
            )
        }
        initJwtForTest()
        application { module() }

        val updateRentalDTO = UpdateRentalDTO(state = 0)

        val response: HttpResponse = client.put("/rentals/99999") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(updateRentalDTO))
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testEndRentalNotFound() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.secret" to "testsecret",
                "ktor.jwt.issuer" to "testissuer",
                "ktor.jwt.audience" to "testaudience",
                "ktor.jwt.realm" to "testrealm",
                "ktor.rdw.apiKey" to "testapikey"
            )
        }
        initJwtForTest()
        application { module() }

        val response: HttpResponse = client.put("/rentals/99999/end")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testGetRentalLocationsNotFound() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.secret" to "testsecret",
                "ktor.jwt.issuer" to "testissuer",
                "ktor.jwt.audience" to "testaudience",
                "ktor.jwt.realm" to "testrealm",
                "ktor.rdw.apiKey" to "testapikey"
            )
        }
        initJwtForTest()
        application { module() }

        val response: HttpResponse = client.get("/rentals/99999/locations")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testUpdateRentalLocationNotFound() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.secret" to "testsecret",
                "ktor.jwt.issuer" to "testissuer",
                "ktor.jwt.audience" to "testaudience",
                "ktor.jwt.realm" to "testrealm",
                "ktor.rdw.apiKey" to "testapikey"
            )
        }
        initJwtForTest()
        application { module() }

        val updateLocationDTO = UpdateRentalLocationDTO(
            date = LocalDateTime.parse("2024-01-01T12:00:00"),
            longitude = 4.9041f,
            latitude = 52.3676f
        )

        val response: HttpResponse = client.put("/rentals/99999/locations/99999") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(updateLocationDTO))
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testDeleteRentalNotFound() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.secret" to "testsecret",
                "ktor.jwt.issuer" to "testissuer",
                "ktor.jwt.audience" to "testaudience",
                "ktor.jwt.realm" to "testrealm",
                "ktor.rdw.apiKey" to "testapikey"
            )
        }
        initJwtForTest()
        application { module() }

        val response: HttpResponse = client.delete("/rentals/99999")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testCreateRentalWithInvalidJson() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.secret" to "testsecret",
                "ktor.jwt.issuer" to "testissuer",
                "ktor.jwt.audience" to "testaudience",
                "ktor.jwt.realm" to "testrealm",
                "ktor.rdw.apiKey" to "testapikey"
            )
        }
        initJwtForTest()
        application { module() }

        val response: HttpResponse = client.post("/rentals") {
            contentType(ContentType.Application.Json)
            setBody("invalid json")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun testCreateRentalHappyPath() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.secret" to "testsecret",
                "ktor.jwt.issuer" to "testissuer",
                "ktor.jwt.audience" to "testaudience",
                "ktor.jwt.realm" to "testrealm",
                "ktor.rdw.apiKey" to "testapikey"
            )
        }
        initJwtForTest()
        application { module() }

        // Setup: Create test data using services directly
        val (userId, carId, rentalId) = setupTestData()

        // Create a second car for this test to avoid conflicts
        val testConfig = MapApplicationConfig(
            "ktor.jwt.secret" to "testsecret",
            "ktor.jwt.issuer" to "testissuer",
            "ktor.jwt.audience" to "testaudience",
            "ktor.jwt.realm" to "testrealm",
            "ktor.rdw.apiKey" to "testapikey"
        )
        DatabaseFactoryImpl.init()
        val serviceFactory = ServiceFactoryImpl(DatabaseFactoryImpl, testConfig)
        
        val secondCarDTO = CreateCarDTO(
            licensePlate = "TEST-${(1000..9999).random()}",
            brand = "BMW",
            model = "X5",
            year = 2024,
            fuelType = 1,
            price = 85000
        )
        val secondCar = serviceFactory.carService.create(secondCarDTO, userId)

        // Now test creating another rental with valid references
        val validRentalDTO = CreateRentalDTO(
            carID = secondCar?.carID ?: 0,
            startLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2024-01-02T10:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            ),
            endLocation = CreateRentalLocationDTO(
                date = LocalDateTime.parse("2024-01-02T18:00:00"),
                longitude = 4.9041f,
                latitude = 52.3676f
            )
        )

        val response: HttpResponse = client.post("/rentals") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(validRentalDTO))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertNotNull(responseBody)
    }

    @Test
    fun testCreateRentalWrongBody() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.secret" to "testsecret",
                "ktor.jwt.issuer" to "testissuer",
                "ktor.jwt.audience" to "testaudience",
                "ktor.jwt.realm" to "testrealm",
                "ktor.rdw.apiKey" to "testapikey"
            )
        }
        initJwtForTest()
        application { module() }

        val response: HttpResponse = client.post("/rentals") {
            contentType(ContentType.Application.Json)
            setBody("""{"invalid": "json", "missing": "required", "fields": true}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun testUpdateRentalHappyPath() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.secret" to "testsecret",
                "ktor.jwt.issuer" to "testissuer",
                "ktor.jwt.audience" to "testaudience",
                "ktor.jwt.realm" to "testrealm",
                "ktor.rdw.apiKey" to "testapikey"
            )
        }
        initJwtForTest()
        application { module() }

        // Setup: Create test data using services directly
        val (userId, carId, rentalId) = setupTestData()

        // Now test updating the rental
        val updateRentalDTO = UpdateRentalDTO(state = 0)

        val response: HttpResponse = client.put("/rentals/$rentalId") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(updateRentalDTO))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertNotNull(responseBody)
    }

    @Test
    fun testUpdateRentalWrongBody() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.secret" to "testsecret",
                "ktor.jwt.issuer" to "testissuer",
                "ktor.jwt.audience" to "testaudience",
                "ktor.jwt.realm" to "testrealm",
                "ktor.rdw.apiKey" to "testapikey"
            )
        }
        initJwtForTest()
        application { module() }

        val response: HttpResponse = client.put("/rentals/1") {
            contentType(ContentType.Application.Json)
            setBody("""{"invalid": "json", "missing": "state", "field": true}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun testEndRentalHappyPath() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.secret" to "testsecret",
                "ktor.jwt.issuer" to "testissuer",
                "ktor.jwt.audience" to "testaudience",
                "ktor.jwt.realm" to "testrealm",
                "ktor.rdw.apiKey" to "testapikey"
            )
        }
        initJwtForTest()
        application { module() }

        // Setup: Create test data using services directly
        val (userId, carId, rentalId) = setupTestData()

        val response: HttpResponse = client.put("/rentals/$rentalId/end")

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertNotNull(responseBody)
    }

    @Test
    fun testGetRentalLocationsHappyPath() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.secret" to "testsecret",
                "ktor.jwt.issuer" to "testissuer",
                "ktor.jwt.audience" to "testaudience",
                "ktor.jwt.realm" to "testrealm",
                "ktor.rdw.apiKey" to "testapikey"
            )
        }
        initJwtForTest()
        application { module() }

        // Setup: Create test data using services directly
        val (userId, carId, rentalId) = setupTestData()

        val response: HttpResponse = client.get("/rentals/$rentalId/locations")

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertNotNull(responseBody)
    }

    @Test
    fun testUpdateRentalLocationHappyPath() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.secret" to "testsecret",
                "ktor.jwt.issuer" to "testissuer",
                "ktor.jwt.audience" to "testaudience",
                "ktor.jwt.realm" to "testrealm",
                "ktor.rdw.apiKey" to "testapikey"
            )
        }
        initJwtForTest()
        application { module() }

        // Setup: Create test data using services directly
        val (userId, carId, rentalId) = setupTestData()

        // Get the rental to find the location IDs
        val testConfig = MapApplicationConfig(
            "ktor.jwt.secret" to "testsecret",
            "ktor.jwt.issuer" to "testissuer",
            "ktor.jwt.audience" to "testaudience",
            "ktor.jwt.realm" to "testrealm",
            "ktor.rdw.apiKey" to "testapikey"
        )
        DatabaseFactoryImpl.init()
        val serviceFactory = ServiceFactoryImpl(DatabaseFactoryImpl, testConfig)
        val rental = serviceFactory.rentalService.getSingle(rentalId)
        val locationId = rental.startRentalLocation.rentalLocationID

        val updateLocationDTO = UpdateRentalLocationDTO(
            date = LocalDateTime.parse("2024-01-01T12:00:00"),
            longitude = 4.9041f,
            latitude = 52.3676f
        )

        val response: HttpResponse = client.put("/rentals/$rentalId/locations/$locationId") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(updateLocationDTO))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertNotNull(responseBody)
    }

    @Test
    fun testUpdateRentalLocationWrongBody() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.secret" to "testsecret",
                "ktor.jwt.issuer" to "testissuer",
                "ktor.jwt.audience" to "testaudience",
                "ktor.jwt.realm" to "testrealm",
                "ktor.rdw.apiKey" to "testapikey"
            )
        }
        initJwtForTest()
        application { module() }

        val response: HttpResponse = client.put("/rentals/1/locations/1") {
            contentType(ContentType.Application.Json)
            setBody("invalid json")
        }

        // Should return 404 since rental with ID 1 doesn't exist in test database
        // The rental check happens before JSON parsing
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testDeleteRentalHappyPath() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.secret" to "testsecret",
                "ktor.jwt.issuer" to "testissuer",
                "ktor.jwt.audience" to "testaudience",
                "ktor.jwt.realm" to "testrealm",
                "ktor.rdw.apiKey" to "testapikey"
            )
        }
        initJwtForTest()
        application { module() }

        // Setup: Create test data using services directly
        val (userId, carId, rentalId) = setupTestData()

        val response: HttpResponse = client.delete("/rentals/$rentalId")

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertNotNull(responseBody)
    }
}