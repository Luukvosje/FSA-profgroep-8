package com.profgroep8

import com.profgroep8.interfaces.repositories.CarRepository
import com.profgroep8.interfaces.repositories.DatabaseFactory
import com.profgroep8.interfaces.repositories.RentalLocationRepository
import com.profgroep8.interfaces.repositories.RentalRepository
import com.profgroep8.interfaces.repositories.UserRepository
import com.profgroep8.interfaces.services.CarService
import com.profgroep8.interfaces.services.ServiceFactory
import com.profgroep8.models.domain.FuelType
import com.profgroep8.models.dto.CarDTO
import com.profgroep8.models.dto.CreateCarDTO
import com.profgroep8.models.dto.CreateUserDTO
import com.profgroep8.models.entity.CarEntity
import com.profgroep8.models.entity.UserEntity
import com.profgroep8.repositories.CarRepositoryImpl
import com.profgroep8.repositories.DatabaseFactoryImpl
import com.profgroep8.repositories.RentalLocationRepositoryImpl
import com.profgroep8.repositories.RentalRepositoryImpl
import com.profgroep8.repositories.UserRepositoryImpl
import com.profgroep8.services.CarServiceImpl
import com.profgroep8.services.ServiceFactoryImpl
import io.ktor.server.config.MapApplicationConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal object MockDatabaseFactoryImpl : DatabaseFactory {
    fun init() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

        transaction {
            // Create Car table
            SchemaUtils.create(CarEntity)
            SchemaUtils.create(UserEntity)
        }
    }

    override val carRepository: CarRepository by lazy { CarRepositoryImpl() }
    override val userRepository: UserRepository by lazy { UserRepositoryImpl() }
    override val rentalRepository: RentalRepository by lazy { RentalRepositoryImpl() }
    override val rentalLocationRepository: RentalLocationRepository by lazy { RentalLocationRepositoryImpl() }
}

internal class CarTest {
    private lateinit var serviceFactory: ServiceFactory

    @Before
    fun setup() {
        // Mock Ktor application config
        val testConfig = MapApplicationConfig(
            "ktor.jwt.secret" to "testsecret",
            "ktor.jwt.issuer" to "testissuer",
            "ktor.jwt.audience" to "testaudience",
            "ktor.jwt.realm" to "testrealm"
        )

        MockDatabaseFactoryImpl.init()

        serviceFactory = ServiceFactoryImpl(MockDatabaseFactoryImpl, testConfig)
    }

    @After
    fun teardown() {
        transaction {
            SchemaUtils.drop(CarEntity)
        }
    }

    @Test
    fun `create car stores car in database`() {
        val userDTO = CreateUserDTO(
            fullName = "Ralph",
            email = "${UUID.randomUUID()}@example.com",
            password = "password",
            phone = "31622830140",
            address = "Points Street",
            zipcode = "2222BB",
            city = "BonusCity",
            countryISO = "NL",
        )

        val userCreated = serviceFactory.userService.register(userDTO)

        val carDTO = CreateCarDTO(
            brand = "Toyota",
            model = "Corolla",
            licensePlate = "ABC123",
            year = 2023,
            price = 0,
            fuelType = 3,
        )
        val created = serviceFactory.carService.create(carDTO)

        assertNotNull(created)
        assertEquals("Toyota", created.brand)
        assertEquals("Corolla", created.model)
        assertEquals("abc123", created.licensePlate)
        assertEquals(2023, created.year)
    }
}
