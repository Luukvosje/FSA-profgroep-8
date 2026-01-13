package com.example.network

import com.example.network.models.domain.CarFuelType
import com.example.network.models.remote.RemoteCar
import com.example.network.services.CarServiceImpl
import com.example.network.services.ApiResult
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class CarIntegrationTest {

    private val json = Json { encodeDefaults = true }

    private fun createMockService(response: String, status: HttpStatusCode = HttpStatusCode.OK): CarServiceImpl {
        val mockEngine = MockEngine { request ->
            respond(
                content = response,
                status = status,
                headers = headersOf("Content-Type", "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json() }
        }

        return CarServiceImpl().apply {
            this.client = client
        }
    }

    @Test
    fun `getAllCars returns list of cars correctly`() = runBlocking {
        val mockCars = listOf(
            RemoteCar(1, "ABC-123", "Toyota", "Corolla", 2020, 0, 20000, 10),
            RemoteCar(2, "XYZ-999", "Tesla", "Model 3", 2022, 2, 45000, 5)
        )

        val service = createMockService(json.encodeToString(mockCars))

        val result: ApiResult<List<com.example.network.models.domain.Car>> = service.getAllCars()

        // Check ApiResult
        if (result is ApiResult.Success) {
            val cars = result.data
            assertEquals(2, cars.size)
            assertEquals(CarFuelType.Gasoline, cars[0].fuelType)
            assertEquals(CarFuelType.Electric, cars[1].fuelType)
        } else {
            throw AssertionError("Expected success result")
        }
    }

    @Test
    fun `getSingleCar returns correct car`() = runBlocking {
        val remoteCar = RemoteCar(1, "ABC-123", "Toyota", "Corolla", 2020, 0, 20000, 10)
        val service = createMockService(json.encodeToString(remoteCar))

        val result = service.getSingleCar(1)

        if (result is ApiResult.Success) {
            val car = result.data
            assertEquals("ABC-123", car.licensePlate)
            assertEquals(CarFuelType.Gasoline, car.fuelType)
        } else {
            throw AssertionError("Expected success result")
        }
    }

    @Test
    fun `searchCars returns filtered cars`() = runBlocking {
        val remoteCars = listOf(
            RemoteCar(1, "ABC-123", "Toyota", "Corolla", 2020, 0, 20000, 10)
        )

        val service = createMockService(json.encodeToString(remoteCars))

        val result = service.searchCars("Corolla")

        if (result is ApiResult.Success) {
            val cars = result.data
            assertEquals(1, cars.size)
            assertEquals("Toyota", cars[0].brand)
        } else {
            throw AssertionError("Expected success result")
        }
    }
}
