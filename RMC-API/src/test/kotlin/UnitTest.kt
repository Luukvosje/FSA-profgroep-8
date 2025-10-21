package com.profgroep8

import com.profgroep8.interfaces.services.CarService
import com.profgroep8.interfaces.services.RentalService
import com.profgroep8.interfaces.services.UserService
import com.profgroep8.models.dto.CreateRentalDTO
import com.profgroep8.models.dto.CreateRentalLocationDTO
import com.profgroep8.models.dto.CreateUserDTO
import com.profgroep8.models.dto.LoginUserDTO
import com.profgroep8.models.dto.UpdateRentalDTO
import com.profgroep8.plugins.JwtConfig
import com.profgroep8.repositories.DatabaseFactoryImpl
import com.profgroep8.services.ServiceFactoryImpl
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import kotlinx.datetime.LocalDateTime
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
        val points = userService.getBonusPoints(user.userId)
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

        val updated = userService.updateBonusPoints(user.userId, 250)

        assertEquals(250, updated.points)
        assertEquals(user.userId, updated.userId)
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

    @Test
    fun `rental service can get all rentals`() {
        val rentals = rentalService.getAll()
        assertNotNull(rentals)
    }

    @Test
    fun `rental service throws NotFoundException for non-existent rental`() {
        assertFailsWith<NotFoundException> {
            rentalService.getSingle(-1)
        }
    }

    @Test
    fun `rental service can create rental with valid data`() {
        // Create test user first
        val userDTO = CreateUserDTO(
            fullName = "Test User",
            email = "test${UUID.randomUUID()}@example.com",
            password = "testpassword123",
            phone = "+31687654321",
            address = "123 Test Street",
            zipcode = "1000AB",
            city = "Amsterdam",
            countryISO = "NL"
        )
        val createdUser = userService.register(userDTO)

        // Create test car
        val carDTO = com.profgroep8.models.dto.CreateCarDTO(
            licensePlate = "TEST-${(1000..9999).random()}",
            brand = "Tesla",
            model = "Model S",
            year = 2025,
            fuelType = 1,
            price = 75000
        )
        val createdCar = carService.create(carDTO)

        // Create rental
        val createRentalDTO = CreateRentalDTO(
            userId = createdUser.userId,
            carId = createdCar.carId,
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

        val createdRental = rentalService.create(createRentalDTO)

        assertNotNull(createdRental)
        assertEquals(createdUser.userId, createdRental.userId)
        assertEquals(createdCar.carId, createdRental.carId)
        assertEquals(4.9041f, createdRental.startRentalLocation.longitude)
        assertEquals(52.3676f, createdRental.startRentalLocation.latitude)
    }

    @Test
    fun `rental service can update rental`() {
        // Create test user first
        val userDTO = CreateUserDTO(
            fullName = "Test User",
            email = "test${UUID.randomUUID()}@example.com",
            password = "testpassword123",
            phone = "+31687654321",
            address = "123 Test Street",
            zipcode = "1000AB",
            city = "Amsterdam",
            countryISO = "NL"
        )
        val createdUser = userService.register(userDTO)

        // Create test car
        val carDTO = com.profgroep8.models.dto.CreateCarDTO(
            licensePlate = "TEST-${(1000..9999).random()}",
            brand = "Tesla",
            model = "Model S",
            year = 2025,
            fuelType = 1,
            price = 75000
        )
        val createdCar = carService.create(carDTO)

        // Create rental
        val createRentalDTO = CreateRentalDTO(
            userId = createdUser.userId,
            carId = createdCar.carId,
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
        val createdRental = rentalService.create(createRentalDTO)

        // Update rental
        val updateRentalDTO = UpdateRentalDTO(state = 0)
        val updatedRental = rentalService.update(createdRental.rentalId, updateRentalDTO)

        assertNotNull(updatedRental)
        assertEquals(createdRental.rentalId, updatedRental.rentalId)
        assertEquals(0, updatedRental.state)
    }

    @Test
    fun `rental service can delete rental`() {
        // Create test user first
        val userDTO = CreateUserDTO(
            fullName = "Test User",
            email = "test${UUID.randomUUID()}@example.com",
            password = "testpassword123",
            phone = "+31687654321",
            address = "123 Test Street",
            zipcode = "1000AB",
            city = "Amsterdam",
            countryISO = "NL"
        )
        val createdUser = userService.register(userDTO)

        // Create test car
        val carDTO = com.profgroep8.models.dto.CreateCarDTO(
            licensePlate = "TEST-${(1000..9999).random()}",
            brand = "Tesla",
            model = "Model S",
            year = 2025,
            fuelType = 1,
            price = 75000
        )
        val createdCar = carService.create(carDTO)

        // Create rental
        val createRentalDTO = CreateRentalDTO(
            userId = createdUser.userId,
            carId = createdCar.carId,
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
        val createdRental = rentalService.create(createRentalDTO)

        // Delete rental
        val deleteResult = rentalService.delete(createdRental.rentalId)

        assertNotNull(deleteResult)
        assertEquals(true, deleteResult)
    }

    @Test
    fun `rental service delete returns false for non-existent rental`() {
        // Try to delete non-existent rental
        val deleteResult = rentalService.delete(-1)

        assertEquals(false, deleteResult)
    }
}