package com.profgroep8.Util

import com.profgroep8.interfaces.utils.RdwFuel
import com.profgroep8.interfaces.utils.RdwFuelTypes
import com.profgroep8.interfaces.utils.RdwVehicle
import com.profgroep8.models.dto.CreateCarDTO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import kotlinx.serialization.json.Json
import java.time.format.DateTimeFormatter

object RdwClient {
    private lateinit var client: HttpClient

    fun init(config: ApplicationConfig) {
        client = HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(DefaultRequest) {
                url("https://opendata.rdw.nl/")
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json)
                headers.append(HttpHeaders.Accept, ContentType.Application.Json)
                headers.append("X-App-Token", config.property("ktor.rdw.apiKey").getString())
            }
        }
    }

    suspend fun getCar(licensePlate: String): CreateCarDTO? {
        val normalized = licensePlate.uppercase().replace("-", "").trim()

        try {
            val response = client.get("resource/m9d7-ebf2.json?kenteken=$normalized")

            if (response.status != HttpStatusCode.OK) {
                throw Error("Call missing")
            }

            val fuelResponse = client.get("resource/8ys7-d773.json?kenteken=$normalized")

            val cars: Array<RdwVehicle> = response.body()
            val car = cars.firstOrNull()

            val fuelInfo: Array<RdwFuel> = fuelResponse.body()
            val fuel = fuelInfo.firstOrNull()

            if (car == null || fuel == null) {
                throw Error("Car missing")
            }

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
            val year = try {
                java.time.LocalDateTime.parse(car.datum_eerste_toelating_dt, formatter).year
            } catch (e: Exception) {
                -1
            }

            return CreateCarDTO(
                licensePlate = car.kenteken,
                brand = car.merk,
                model = car.handelsbenaming,
                year = year,
                fuelType = RdwFuelTypes.fromString(fuel.brandstof_omschrijving) ?: 0,
                price = 0,
            )
        } catch (e: Exception) {
            return null
        }
    }
}