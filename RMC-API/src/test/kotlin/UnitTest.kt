package com.profgroep8

import com.profgroep8.interfaces.services.CarService
import com.profgroep8.interfaces.services.RentalService
import com.profgroep8.interfaces.services.UserService
import com.profgroep8.models.dto.CreateUserDTO
import com.profgroep8.models.dto.LoginUserDTO
import com.profgroep8.plugins.JwtConfig
import com.profgroep8.repositories.DatabaseFactoryImpl
import com.profgroep8.services.ServiceFactoryImpl
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.plugins.BadRequestException
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class UnitTest {

    private lateinit var userService: UserService
    private lateinit var rentalService: RentalService
    private lateinit var carService: CarService

    @Before
    fun setup() {
        JwtConfig.init(
            MapApplicationConfig(
                "ktor.jwt.secret" to "testsecret",
                "ktor.jwt.issuer" to "testissuer",
                "ktor.jwt.audience" to "testaudience",
                "ktor.jwt.realm" to "testrealm",
                "ktor.rdw.apiKey" to "dummykey"
            )
        )

        // Initialize database and services for all tests
        DatabaseFactoryImpl.init()
        val testConfig = MapApplicationConfig(
            "ktor.jwt.secret" to "testsecret",
            "ktor.jwt.issuer" to "testissuer",
            "ktor.jwt.audience" to "testaudience",
            "ktor.jwt.realm" to "testrealm",
            "ktor.rdw.apiKey" to "testapikey"
        )
        val serviceFactory = ServiceFactoryImpl(DatabaseFactoryImpl, testConfig)
        userService = serviceFactory.userService
        rentalService = serviceFactory.rentalService
        carService = serviceFactory.carService
    }

    @Test
    fun `register fails when email already exists`() {
        val email = "existing@example.com"
        val dto = CreateUserDTO("Paul De Mast", email, "test123", "+31687654321", "456 Street", "2000CD", "Rotterdam", "NL")
        try {
            userService.register(dto)
        } catch (_: Exception) {}
        // 2nd registration with same email must fail
        assertFailsWith<BadRequestException> {
            userService.register(dto)
        }
    }

    @Test
    fun `login fails with wrong password`() {
        val email = "fail@example.com"
        val dto = CreateUserDTO("Test User", email, "test123", "+31000000000", "Any Street", "4444AA", "FakeCity", "NL")
        try { userService.register(dto) } catch (_: Exception) {}
        assertFailsWith<BadRequestException> {
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
        val user = userService.register(dto)
        val response = userService.login(LoginUserDTO(email, "testPass123"))
        assertNotNull(response.token)
        assertEquals(user.email, response.user.email)
    }

    @Test
    fun `new user starts with 0 bonus points`() {
        val email = "bonus_${UUID.randomUUID()}@example.com"
        val dto = CreateUserDTO(
            fullName = "Bonus Test",
            email = email,
            password = "bonus123",
            phone = "+31600000000",
            address = "Points Street",
            zipcode = "2222BB",
            city = "BonusCity",
            countryISO = "NL"
        )
        val user = userService.register(dto)
        val points = userService.getBonusPoints(user.userID)
        assertEquals(0, points)
    }

    @Test
    fun `update bonus points changes the value`() {
        val email = "bonusupd_${UUID.randomUUID()}@example.com"
        val dto = CreateUserDTO(
            fullName = "Bonus Update",
            email = email,
            password = "bonusupd123",
            phone = "+31611111111",
            address = "Update Street",
            zipcode = "3333CC",
            city = "UpdateCity",
            countryISO = "NL"
        )
        val user = userService.register(dto)

        val updated = userService.updateBonusPoints(user.userID, 250)

        assertEquals(250, updated.points)
        assertEquals(user.userID, updated.userID)
    }

    @Test
    fun `get bonus points fails for non existing user`() {
        assertFailsWith<BadRequestException> {
            userService.getBonusPoints(-1)
        }
    }

    @Test
    fun `update bonus points fails for non existing user`() {
        assertFailsWith<BadRequestException> {
            userService.updateBonusPoints(-1, 500)
        }
    }
}