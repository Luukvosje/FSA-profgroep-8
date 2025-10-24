package com.profgroep8.integrationtests

import com.profgroep8.integrations.BaseIntegrationTest
import com.profgroep8.models.dto.CreateUserDTO
import com.profgroep8.models.dto.LoginUserDTO
import com.profgroep8.models.dto.UserDTO
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.*

class UserIntegrationTest : BaseIntegrationTest() {

    @Test
    fun testRegisterUser() = runTest {
        val newUser = CreateUserDTO(
            fullName = "Paul De Mast",
            email = "paul_${System.currentTimeMillis()}@example.com",
            password = "test123",
            phone = "+31687654321",
            address = "456 Street",
            zipcode = "2000CD",
            city = "Rotterdam",
            countryISO = "NL"
        )

        val response = post("/users/register") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(CreateUserDTO.serializer(), newUser))
        }

        assertEquals(HttpStatusCode.Created, response.status)

        val createdUser = Json.decodeFromString(UserDTO.serializer(), response.bodyAsText())
        assertNotNull(createdUser)
        assertEquals("Paul De Mast", createdUser.fullName)
        assertEquals(newUser.email, createdUser.email)
    }

    @Test
    fun testLoginUser() = runTest {
        val email = "login_${System.currentTimeMillis()}@example.com"
        val createUser = CreateUserDTO(
            fullName = "Login User",
            email = email,
            password = "test123",
            phone = "+31600000000",
            address = "Test Street",
            zipcode = "1234AB",
            city = "TestCity",
            countryISO = "NL"
        )

        post("/users/register") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(CreateUserDTO.serializer(), createUser))
        }

        val loginDTO = LoginUserDTO(email, "test123")
        val response = post("/users/login") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(LoginUserDTO.serializer(), loginDTO))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertNotNull(body)
        assert(body.contains("token"))
    }

    @Test
    fun testGetLoggedInUser() = runTest {
        val token = generateToken() // Uses your existing method

        val response = get("/users/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertNotNull(body)
        assert(body.contains("johndoe@example.com"))
    }

    @Test
    fun testGetBonusPoints() = runTest {
        val token = generateToken() // Uses your existing method

        val response = get("/users/1/bonuspoints") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertNotNull(body)
        assert(body.contains("bonusPoints"))
    }

    @Test
    fun testUpdateBonusPoints() = runTest {
        val token = generateToken() // Uses your existing method

        val response = put("/users/1/bonuspoints") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{ "points": 200 }""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertNotNull(body)
        assert(body.contains("200"))
    }

    @Test
    fun testGetLoggedInUser_Unauthorized() = runTest {
        val response = get("/users/me")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun testGetBonusPoints_InvalidUser() = runTest {
        val token = generateToken() // Uses your existing method

        val response = get("/users/9999/bonuspoints") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun testUpdateBonusPoints_InvalidUser() = runTest {
        val token = generateToken() // Uses your existing method

        val response = put("/users/9999/bonuspoints") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{ "points": 100 }""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}