package com.profgroep8.integrations

import com.profgroep8.interfaces.services.ServiceFactory
import com.profgroep8.mocks.MockDatabaseFactoryImpl
import com.profgroep8.models.dto.CreateUserDTO
import com.profgroep8.models.entity.CarEntity
import com.profgroep8.models.entity.RentalEntity
import com.profgroep8.models.entity.RentalLocationsEntity
import com.profgroep8.models.entity.UserEntity
import com.profgroep8.module
import com.profgroep8.plugins.JwtConfig
import com.profgroep8.services.ServiceFactoryImpl
import io.ktor.client.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Before

open class BaseIntegrationTest {
    private val testConfig = MapApplicationConfig(
        "ktor.jwt.secret" to "testsecret",
        "ktor.jwt.issuer" to "testissuer",
        "ktor.jwt.audience" to "testaudience",
        "ktor.jwt.realm" to "testrealm",
        "ktor.rdw.apiKey" to "testapikey"
    )

    protected lateinit var serviceFactory: ServiceFactory

    protected fun runTest(block: suspend HttpClient.() -> Unit) = testApplication {
        environment { config = testConfig }
        application { module() }
        client.block()
    }

    protected fun generateToken(): String {
        val user = serviceFactory.userService.register(
            CreateUserDTO(
                fullName = "John Doe",
                email = "johndoe@example.com",
                password = "test123",
                phone = "+31687654321",
                address = "456 Street",
                zipcode = "2000CD",
                city = "Rotterdam",
                countryISO = "NL"
            )
        )

        return JwtConfig.generateToken(user.userID.toString(), "johndoe@example.com")
    }

    @Before
    fun setup() {
        MockDatabaseFactoryImpl.init()
        JwtConfig.init(testConfig)
        serviceFactory = ServiceFactoryImpl(MockDatabaseFactoryImpl, testConfig)
    }

    @After
    fun teardown() {
        transaction {
            SchemaUtils.drop(
                RentalEntity,
                CarEntity,
                RentalLocationsEntity,
                UserEntity
            )
        }
    }
}