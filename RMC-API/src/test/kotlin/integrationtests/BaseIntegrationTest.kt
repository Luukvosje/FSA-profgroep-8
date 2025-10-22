package com.profgroep8.integrations

import com.profgroep8.interfaces.services.ServiceFactory
import com.profgroep8.mocks.MockDatabaseFactoryImpl
import com.profgroep8.models.dto.CreateCarDTO
import com.profgroep8.models.dto.CreateRentalDTO
import com.profgroep8.models.dto.CreateRentalLocationDTO
import com.profgroep8.models.dto.CreateUserDTO
import com.profgroep8.models.dto.UpdateRentalDTO
import com.profgroep8.models.entity.CarEntity
import com.profgroep8.models.entity.RentalEntity
import com.profgroep8.models.entity.RentalLocationsEntity
import com.profgroep8.models.entity.UserEntity
import com.profgroep8.module
import com.profgroep8.plugins.JwtConfig
import com.profgroep8.services.ServiceFactoryImpl
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.testApplication
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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

    protected fun generateToken(): String =
        JwtConfig.generateToken("1", "johndoe@example.com")

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