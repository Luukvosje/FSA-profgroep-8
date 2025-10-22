package com.profgroep8

import com.profgroep8.interfaces.services.CarService
import com.profgroep8.interfaces.services.RentalService
import com.profgroep8.interfaces.services.ServiceFactory
import com.profgroep8.interfaces.services.UserService
import com.profgroep8.mocks.MockDatabaseFactoryImpl
import com.profgroep8.models.domain.Car
import com.profgroep8.models.domain.FuelType
import com.profgroep8.models.dto.CalculateCarRequestDTO
import com.profgroep8.models.dto.CarDTO
import com.profgroep8.models.dto.CreateRentalDTO
import com.profgroep8.models.dto.CreateRentalLocationDTO
import com.profgroep8.models.dto.CreateUserDTO
import com.profgroep8.models.dto.LoginUserDTO
import com.profgroep8.models.dto.UpdateRentalDTO
import com.profgroep8.plugins.JwtConfig
import com.profgroep8.repositories.DatabaseFactoryImpl
import com.profgroep8.services.CarServiceImpl
import com.profgroep8.services.ServiceFactoryImpl
import io.ktor.client.HttpClient
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.testing.testApplication
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

open class BaseUnitTest {
    private val testConfig = MapApplicationConfig(
        "ktor.jwt.secret" to "testsecret",
        "ktor.jwt.issuer" to "testissuer",
        "ktor.jwt.audience" to "testaudience",
        "ktor.jwt.realm" to "testrealm",
        "ktor.rdw.apiKey" to "testapikey"
    )

    protected lateinit var serviceFactory: ServiceFactory

    @Before
    fun setup() {
        MockDatabaseFactoryImpl.init()
        JwtConfig.init(testConfig)
        serviceFactory = ServiceFactoryImpl(MockDatabaseFactoryImpl, testConfig)
    }

    @After
    fun teardown() {
        transaction {
            SchemaUtils.drop(com.profgroep8.models.entity.RentalEntity,
                com.profgroep8.models.entity.CarEntity,
                com.profgroep8.models.entity.RentalLocationsEntity,
                com.profgroep8.models.entity.UserEntity)
        }
    }
}