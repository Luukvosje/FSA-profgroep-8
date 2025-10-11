package com.profgroep8.Util

import com.profgroep8.interfaces.utils.RdwFuel
import com.profgroep8.interfaces.utils.RdwVehicle
import com.profgroep8.models.dto.CreateCarDTO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import kotlinx.serialization.json.Json
import java.time.format.DateTimeFormatter

class RdwImpl(config: ApplicationConfig) {
    private val apiKey = config.property("ktor.rdw.apiKey").getString()
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }
    }


    suspend fun getCar(licensePlate: String): CreateCarDTO? {
        val normalized = licensePlate.uppercase().replace("-", "").trim()

        //Url for basic information
        val basicUrl = "https://opendata.rdw.nl/resource/m9d7-ebf2.json?kenteken=$normalized"

        //Url for fuel information
        val fuelUrl = "https://opendata.rdw.nl/resource/8ys7-d773.json?kenteken=$normalized"

        try {
            val response = client.get(basicUrl) {
                header("X-App-Token", apiKey)
                header("Accept", "application/json")
            }

            if(response.status != HttpStatusCode.OK) {
                throw Error("Call missing")
            }

            val fuelResponse = client.get(fuelUrl) {
                header("X-App-Token", apiKey)
                header("Accept", "application/json")
            }

            val cars: Array<RdwVehicle> = response.body()
            val car = cars.firstOrNull()

            val fuelInfo: Array<RdwFuel> = response.body()
            val fuel = fuelInfo.firstOrNull()

            if(car == null || fuel == null) {
                throw Error("Car missing")
            }

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
            val year = try {
                java.time.LocalDateTime.parse(car.datum_eerste_toelating_dt, formatter).year
            } catch (e: Exception) {
                0
            }

            return CreateCarDTO(
                licensePlate = car.kenteken,
                brand = car.merk,
                model = car.handelsbenaming,
                year = year,
                fuelType = fuel.brandstof_volgnummer.toInt(),
                price = 0,
            )
        } catch (e: Exception) {
            return null
        }
    }

}