package com.profgroep8

import com.profgroep8.models.dto.CreateCarDTO
import com.profgroep8.models.dto.CreateUserDTO
import com.profgroep8.models.dto.LoginUserDTO
import com.profgroep8.plugins.JwtConfig
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import java.util.UUID

class ApplicationTest {

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
}