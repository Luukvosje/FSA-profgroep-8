package com.profgroep8.rmc_app.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.serializer

@Serializable
data class NominatimResult(
    val lat: String,
    val lon: String,
    val display_name: String? = null
)

object NominatimGeocoder {
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(jsonParser)
        }
    }

    suspend fun geocodeAddress(address: String): Pair<Float, Float>? = withContext(Dispatchers.IO) {
        try {
            val response = client.get("https://nominatim.openstreetmap.org/search") {
                url {
                    parameters.append("q", address)
                    parameters.append("format", "json")
                    parameters.append("limit", "1")
                }
                header("User-Agent", "RMC-Android-App")
            }

            val responseBody: String = response.bodyAsText()
            val results: List<NominatimResult> = jsonParser.decodeFromString(
                ListSerializer(serializer<NominatimResult>()),
                responseBody
            )

            if (results.isNotEmpty()) {
                val result: NominatimResult = results.first()
                Pair(result.lat.toFloat(), result.lon.toFloat())
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

