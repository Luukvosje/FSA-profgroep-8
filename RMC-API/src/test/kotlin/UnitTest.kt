package com.profgroep8

import com.profgroep8.interfaces.repositories.UserRepository
import com.profgroep8.interfaces.services.UserService
import com.profgroep8.models.dto.CreateUserDTO
import com.profgroep8.models.dto.LoginUserDTO
import com.profgroep8.models.dto.UserDTO
import com.profgroep8.plugins.JwtConfig
import com.profgroep8.services.UserServiceImpl
import io.ktor.server.config.MapApplicationConfig
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class UnitTest {

    private lateinit var userService: UserService

    @Before
    fun setup() {
        userService = UserServiceImpl()
        JwtConfig.init(
            MapApplicationConfig(
                "ktor.jwt.secret" to "testsecret",
                "ktor.jwt.issuer" to "testissuer",
                "ktor.jwt.audience" to "testaudience",
                "ktor.jwt.realm" to "testrealm"
            )
        )
    }

    @Test
    fun `register fails when email already exists`() {
        // Since we can't mock DB state, this must be tested via integration (see ApplicationTest)
        // So we expect BadRequestException if duplicate registration happens (integration tests)
        val email = "existing@example.com"
        val dto = CreateUserDTO("Paul De Mast", email, "test123", "+31687654321", "456 Street", "2000CD", "Rotterdam", "NL")
        // 1st registration should pass:
        try {
            userService.register(dto)
        } catch (_: Exception) {}
        // 2nd registration with same email must fail
        assertFailsWith<io.ktor.server.plugins.BadRequestException> {
            userService.register(dto)
        }
    }

    @Test
    fun `login fails with wrong password`() {
        val email = "fail@example.com"
        val dto = CreateUserDTO("Test User", email, "test123", "+31000000000", "Any Street", "4444AA", "FakeCity", "NL")
        // Create the user with known password:
        try { userService.register(dto) } catch (_: Exception) {}
        // Attempt login with wrong password:
        assertFailsWith<io.ktor.server.plugins.BadRequestException> {
            userService.login(LoginUserDTO(email, "wrongPass"))
        }
    }
    
    @Test
    fun `login works with correct credentials`() {
        val email = "login_${UUID.randomUUID()}@example.com"
        val dto = CreateUserDTO(
            fullName = "Login Test",
            email = email,
            password = "testPass123",
            phone = "+31655555555",
            address = "Street 1",
            zipcode = "1111AA",
            city = "AnyCity",
            countryISO = "NL"
        )
        // Registration (setup)
        val user = userService.register(dto)
        val loginDto = LoginUserDTO(email, "testPass123")
        val response = userService.login(loginDto)
        assertNotNull(response.token)
        assertEquals(user.email, response.user.email)
    }

    // TODO: The following tests require DB state/mocking not available with current service pattern:
    // - register user successfully
    // - get bonus points
    // - update bonus points
    // - get user by id
    // See ApplicationTest for full integration coverage.
}