package com.profgroep8.unittests


import com.profgroep8.BaseUnitTest
import com.profgroep8.models.dto.CreateUserDTO
import com.profgroep8.models.dto.LoginUserDTO
import io.ktor.server.plugins.BadRequestException
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class UserUnitTest  : BaseUnitTest(){
    @Test
    fun testRegisterFailsWhenEmailAlreadyExists() {
        val email = "existing@example.com"
        val dto = CreateUserDTO("Paul De Mast", email, "test123", "+31687654321", "456 Street", "2000CD", "Rotterdam", "NL")
        try { serviceFactory.userService.register(dto) } catch (_: Exception) {}

        assertFailsWith<BadRequestException> {
            serviceFactory.userService.register(dto)
        }
    }

    @Test
    fun testLoginFailsWithWrongPassword() {
        val email = "fail@example.com"
        val dto = CreateUserDTO("Test User", email, "test123", "+31000000000", "Any Street", "4444AA", "FakeCity", "NL")
        try { serviceFactory.userService.register(dto) } catch (_: Exception) {}

        assertFailsWith<BadRequestException> {
            serviceFactory.userService.login(LoginUserDTO(email, "wrongPass"))
        }
    }

    @Test
    fun testLoginWorksWithCorrectCredentials() {
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
        val user = serviceFactory.userService.register(dto)
        val response = serviceFactory.userService.login(LoginUserDTO(email, "testPass123"))

        assertNotNull(response.token)
        assertEquals(user.email, response.user.email)
    }

    @Test
    fun testNewUserStartsWithZeroBonusPoints() {
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
        val user = serviceFactory.userService.register(dto)
        val points = serviceFactory.userService.getBonusPoints(user.userID)
        assertEquals(0, points)
    }

    @Test
    fun testUpdateBonusPointsChangesValue() {
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
        val user = serviceFactory.userService.register(dto)
        val updated = serviceFactory.userService.updateBonusPoints(user.userID, 250)

        assertEquals(250, updated.points)
        assertEquals(user.userID, updated.userID)
    }

    @Test
    fun testGetBonusPointsFailsForNonExistingUser() {
        assertFailsWith<BadRequestException> {
            serviceFactory.userService.getBonusPoints(-1)
        }
    }

    @Test
    fun testUpdateBonusPointsFailsForNonExistingUser() {
        assertFailsWith<BadRequestException> {
            serviceFactory.userService.updateBonusPoints(-1, 500)
        }
    }
}