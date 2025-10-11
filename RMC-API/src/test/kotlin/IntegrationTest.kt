package com.profgroep8

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
import org.junit.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class IntegrationTest{
private fun testConfig() = MapApplicationConfig(
    "ktor.jwt.secret" to "testsecret",
    "ktor.jwt.issuer" to "testissuer",
    "ktor.jwt.audience" to "testaudience",
    "ktor.jwt.realm" to "testrealm"
)

@Test
fun testRegisterUser() = testApplication {
    environment { config = testConfig() }
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
    environment { config = testConfig() }
    application { module() }

    // Register user before login
    val email = "login_" + UUID.randomUUID().toString() + "@example.com"
    val createUserDTO = CreateUserDTO(
        fullName = "Login User",
        email = email,
        password = "test123",
        phone = "+31611112222",
        address = "789 Street",
        zipcode = "3000EF",
        city = "Utrecht",
        countryISO = "NL"
    )
    client.post("/users/register") {
        contentType(ContentType.Application.Json)
        setBody(Json.encodeToString(createUserDTO))
    }

    val loginUserDTO = LoginUserDTO(email = email, password = "test123")
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
    environment { config = testConfig() }
    JwtConfig.init(testConfig())
    application { module() }

    // Step 1: Register a real user
    val email = "meuser_" + UUID.randomUUID().toString() + "@example.com"
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
    client.post("/users/register") {
        contentType(ContentType.Application.Json)
        setBody(Json.encodeToString(createUserDTO))
    }

    // Step 2: Log in to get a valid token
    val loginUserDTO = LoginUserDTO(email = email, password = "test123")
    val loginResponse: HttpResponse = client.post("/users/login") {
        contentType(ContentType.Application.Json)
        setBody(Json.encodeToString(loginUserDTO))
    }
    val loginBody = loginResponse.bodyAsText()
    assert(loginBody.contains("token"))
    val token = Regex("\"token\":\"([^\"]+)\"").find(loginBody)!!.groupValues[1]

    // Step 3: Call /users/me with the token
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
    environment { config = testConfig() }
    JwtConfig.init(testConfig())
    application { module() }

    val token = JwtConfig.generateToken("1", "bonususer@example.com")
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
    environment { config = testConfig() }
    JwtConfig.init(testConfig())
    application { module() }

    val token = JwtConfig.generateToken("1", "bonususer@example.com")
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