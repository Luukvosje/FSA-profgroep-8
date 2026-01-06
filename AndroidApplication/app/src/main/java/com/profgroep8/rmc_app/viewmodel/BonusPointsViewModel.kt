package com.profgroep8.rmc_app.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.network.interfaces.services.ServiceFactory
import com.example.network.services.ApiResult
import com.profgroep8.rmc_app.ui.screens.bonuspoints.BonusPointsUIState
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import kotlin.math.*
import kotlin.random.Random

/**
 * If ORS gives 403: we automatically fallback to OSRM (free, no key).
 * If you DO have a real ORS key, put it here (it usually looks like a long hex/string, not a JWT).
 */
private const val ORS_API_KEY: String = ""

private data class LatLon(val lat: Double, val lon: Double)

class BonusPointsViewModel(private val serviceFactory: ServiceFactory) : BaseViewModel() {

    private val http = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }
    private val json = Json { ignoreUnknownKeys = true }

    private val _uiState = MutableStateFlow(BonusPointsUIState())
    val uiState: StateFlow<BonusPointsUIState> = _uiState.asStateFlow()

    private var apiRefreshJob: Job? = null
    private var simJob: Job? = null

    private var cachedUserId: Int? = null
    private var serverPoints: Int = 0

    private var simBonus: Int = 0
    private var basePointsAtSimStart: Int = 0

    private var routeCoords: List<LatLon> = emptyList()
    private var routeIndex: Int = 0

    // ----- physics -----
    private val vehicleMass = 1200.0  // Lighter car for faster acceleration
    private val wheelRadius = 0.32
    private val maxEngineTorque = 500.0  // Much higher torque for realistic acceleration
    private val gearRatios = listOf(3.6, 2.1, 1.4, 1.0, 0.8)
    private val finalDrive = 3.4

    private var currentGear = 1
    private var engineRpm = 900.0
    private var speed = 0.0
    private var throttle = 0.0
    private var brake = 0.0
    private var acceleration = 0.0
    private var score = 100.0

    // ✅ Dynamic speed target
    private var targetSpeedOffset = 0.0
    private var ticksSinceLastChange = 0

    init {
        startApiRefresh()
    }

    fun onStartAddressChanged(v: String) = _uiState.update { it.copy(startAddress = v) }
    fun onEndAddressChanged(v: String) = _uiState.update { it.copy(endAddress = v) }

    // ---------------------------
    // refresh DB bonuspoints each second
    // ---------------------------
    private fun startApiRefresh() {
        if (apiRefreshJob != null) return
        apiRefreshJob = viewModelScope.launch {
            refreshServerPoints()
            while (true) {
                delay(1000)
                refreshServerPoints()
            }
        }
    }

    private suspend fun refreshServerPoints() {
        val userId = cachedUserId ?: run {
            val me = serviceFactory.userService.getMe()
            if (me is ApiResult.Error) {
                _uiState.update { it.copy(isUnauthorized = true, isLoading = false) }
                return
            }
            val id = (me as ApiResult.Success).data.userID
            cachedUserId = id
            id
        }

        when (val pointsRes = serviceFactory.userService.getBonusPoints(userId)) {
            is ApiResult.Success -> {
                serverPoints = pointsRes.data
                _uiState.update { it.copy(bonusPoints = serverPoints, isUnauthorized = false, errorMessage = null) }
            }
            is ApiResult.Error -> {
                _uiState.update { it.copy(errorMessage = pointsRes.exception.toString()) }
            }
        }
    }

    // ---------------------------
    // Simulation
    // ---------------------------
    fun startSimulation() {
        if (simJob != null) return

        val startAddr = uiState.value.startAddress.trim()
        val endAddr = uiState.value.endAddress.trim()
        if (startAddr.isBlank() || endAddr.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please fill in Start and Destination addresses.") }
            return
        }

        simJob = viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null, simulationStatus = "Geocoding...") }

                val start = geocodeNominatim(startAddr)
                delay(1100) // Nominatim politeness
                val end = geocodeNominatim(endAddr)

                _uiState.update { it.copy(simulationStatus = "Routing...") }

                // ✅ ORS (if key) -> fallback OSRM (free)
                routeCoords = fetchRouteWithFallback(start, end)
                if (routeCoords.size < 2) throw IllegalStateException("Route not found.")

                resetSim()
                routeIndex = 0
                basePointsAtSimStart = serverPoints
                simBonus = 0

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSimulationRunning = true,
                        simulationStatus = "Driving route..."
                    )
                }

                while (true) {
                    if (!uiState.value.isSimulationRunning) break

                    val current = routeCoords[routeIndex]
                    val dest = routeCoords.last()

                    val limit = fetchSpeedLimit(current) ?: 50
                    driverModelBySpeedLimit(limit)
                    vehiclePhysicsTick()
                    scoringModel(limit)
                    publishTexts(limit)

                    val metersPerSec = speed / 3.6
                    val metersThisTick = metersPerSec * 0.1
                    advanceAlongRoute(metersThisTick)

                    val remaining = haversineMeters(routeCoords[routeIndex], dest)
                    if (routeIndex >= routeCoords.lastIndex || remaining < 10.0) {
                        stopSimulationInternal(finalDbUpdate = true)
                        _uiState.update { it.copy(simulationStatus = "Arrived ✅ Simulation finished.") }
                        break
                    }

                    delay(100)
                }
            } catch (e: Exception) {
                stopSimulationInternal(finalDbUpdate = false)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: e.toString(),
                        simulationStatus = ""
                    )
                }
            }
        }
    }

    fun stopSimulation() {
        stopSimulationInternal(finalDbUpdate = true)
    }

    private fun stopSimulationInternal(finalDbUpdate: Boolean) {
        simJob?.cancel()
        simJob = null

        viewModelScope.launch {
            if (finalDbUpdate) {
                val userId = cachedUserId
                if (userId != null) {
                    val finalPoints = basePointsAtSimStart + simBonus
                    serviceFactory.userService.updateBonusPoints(userId, finalPoints)
                    serverPoints = finalPoints
                    _uiState.update { it.copy(bonusPoints = finalPoints) }
                }
            }
            _uiState.update { it.copy(isSimulationRunning = false) }
        }
    }

    fun logout() {
        stopSimulationInternal(finalDbUpdate = false)
        serviceFactory.userService.logout()
        cachedUserId = null
        _uiState.update { it.copy(isUnauthorized = true) }
    }

    // ==========================================================
    // External APIs
    // ==========================================================
    private suspend fun geocodeNominatim(query: String): LatLon {
        val text = http.get("https://nominatim.openstreetmap.org/search") {
            parameter("q", query)
            parameter("format", "json")
            parameter("limit", "1")
            header(HttpHeaders.UserAgent, "rmc-app/1.0 (student project)")
        }.bodyAsText()

        val root = json.parseToJsonElement(text)
        val arr = root.jsonArray
        val first = arr.firstOrNull()?.jsonObject
            ?: throw IllegalStateException("Address not found: $query")

        val lat = first["lat"]?.jsonPrimitive?.content?.toDoubleOrNull()
            ?: throw IllegalStateException("Invalid geocode response (lat).")
        val lon = first["lon"]?.jsonPrimitive?.content?.toDoubleOrNull()
            ?: throw IllegalStateException("Invalid geocode response (lon).")

        return LatLon(lat = lat, lon = lon)
    }

    /**
     * ✅ Routing:
     * - Try ORS only if the key seems present
     * - If ORS responds 401/403, fallback to OSRM (free, no key)
     */
    private suspend fun fetchRouteWithFallback(start: LatLon, end: LatLon): List<LatLon> {
        val hasOrsKey = ORS_API_KEY.isNotBlank()

        if (hasOrsKey) {
            try {
                return fetchRouteORS(start, end)
            } catch (e: Exception) {
                // If ORS fails, fallback
                _uiState.update {
                    it.copy(simulationStatus = "ORS blocked (403). Using free OSRM routing...")
                }
            }
        }

        return fetchRouteOSRM(start, end)
    }

    /**
     * ORS routing (can fail with 403 if key is wrong)
     */
    private suspend fun fetchRouteORS(start: LatLon, end: LatLon): List<LatLon> {
        val bodyJson = buildJsonObject {
            put("coordinates", buildJsonArray {
                add(buildJsonArray { add(start.lon); add(start.lat) })
                add(buildJsonArray { add(end.lon); add(end.lat) })
            })
        }

        val response: HttpResponse = http.post("https://api.openrouteservice.org/v2/directions/driving-car") {
            parameter("geometry_format", "geojson")
            header(HttpHeaders.Authorization, ORS_API_KEY)
            header(HttpHeaders.Accept, "application/json")
            header(HttpHeaders.UserAgent, "rmc-app/1.0 (student project)")
            contentType(ContentType.Application.Json)
            setBody(bodyJson.toString())
        }

        val text = response.bodyAsText()
        if (!response.status.isSuccess()) {
            val msg = extractOrsErrorMessage(text)
            // Throw to trigger fallback
            throw IllegalStateException("ORS error ${response.status.value}: $msg")
        }

        val root = json.parseToJsonElement(text).jsonObject
        val routes = root["routes"]?.jsonArray ?: throw IllegalStateException("ORS: no routes in response.")
        val coords = routes.first().jsonObject["geometry"]!!.jsonObject["coordinates"]!!.jsonArray

        return coords.mapNotNull { item ->
            val pair = item.jsonArray
            if (pair.size < 2) null
            else LatLon(
                lat = pair[1].jsonPrimitive.double,
                lon = pair[0].jsonPrimitive.double
            )
        }
    }

    /**
     * ✅ OSRM routing (FREE, no key)
     * https://router.project-osrm.org/route/v1/driving/lon,lat;lon,lat?overview=full&geometries=geojson
     */
    private suspend fun fetchRouteOSRM(start: LatLon, end: LatLon): List<LatLon> {
        val url =
            "https://router.project-osrm.org/route/v1/driving/" +
                    "${start.lon},${start.lat};${end.lon},${end.lat}"

        val text = http.get(url) {
            parameter("overview", "full")
            parameter("geometries", "geojson")
            header(HttpHeaders.UserAgent, "rmc-app/1.0 (student project)")
        }.bodyAsText()

        val root = json.parseToJsonElement(text).jsonObject

        val code = root["code"]?.jsonPrimitive?.contentOrNull
        if (code != null && code != "Ok") {
            throw IllegalStateException("OSRM error: $code")
        }

        val routes = root["routes"]?.jsonArray ?: throw IllegalStateException("OSRM: no routes.")
        val geometry = routes.first().jsonObject["geometry"]?.jsonObject
            ?: throw IllegalStateException("OSRM: no geometry.")
        val coords = geometry["coordinates"]?.jsonArray ?: throw IllegalStateException("OSRM: no coordinates.")

        return coords.mapNotNull { item ->
            val pair = item.jsonArray
            if (pair.size < 2) null
            else LatLon(
                lat = pair[1].jsonPrimitive.double,
                lon = pair[0].jsonPrimitive.double
            )
        }
    }

    private fun extractOrsErrorMessage(body: String): String {
        return try {
            val el = json.parseToJsonElement(body)
            val obj = el.jsonObject
            val errorObj = obj["error"]?.jsonObject
            val msg1 = errorObj?.get("message")?.jsonPrimitive?.contentOrNull
            val msg2 = obj["message"]?.jsonPrimitive?.contentOrNull
            msg1 ?: msg2 ?: body.take(300)
        } catch (_: Exception) {
            body.take(300)
        }
    }

    /**
     * ✅ FIXED: Fetch speed limit using correct Overpass API query format
     */
    private suspend fun fetchSpeedLimit(point: LatLon): Int? {
        return try {
            val query = "[out:json];way(around:25,${point.lat},${point.lon})[highway][maxspeed];out tags 1;"

            val text = http.post("https://overpass-api.de/api/interpreter") {
                contentType(ContentType.Text.Plain)
                setBody(query)
                header(HttpHeaders.UserAgent, "rmc-app/1.0 (student project)")
            }.bodyAsText()

            val root = json.parseToJsonElement(text).jsonObject
            val elements = root["elements"]?.jsonArray ?: return null

            val raw = elements.firstNotNullOfOrNull { el ->
                val tags = el.jsonObject["tags"]?.jsonObject
                tags?.get("maxspeed")?.jsonPrimitive?.contentOrNull
            }

            parseMaxspeedKmh(raw)
        } catch (e: Exception) {
            // If speed limit lookup fails, return null (will use default 50)
            null
        }
    }

    // ---------------------------
    // Simulation logic
    // ---------------------------
    private fun resetSim() {
        speed = 0.0
        engineRpm = 900.0
        currentGear = 1
        throttle = 0.0
        brake = 0.0
        acceleration = 0.0
        score = 100.0
        targetSpeedOffset = Random.nextDouble(-5.0, -1.0)
        ticksSinceLastChange = 0
    }

    /**
     * ✅ FIXED: Realistic driver behavior that actually reaches speed limit
     * Target speed is (limit - 1 to 5) km/h, changes every 2-4 seconds
     */
    private fun driverModelBySpeedLimit(limitKmh: Int) {
        // Change target offset randomly every 2-4 seconds
        ticksSinceLastChange++
        if (ticksSinceLastChange > Random.nextInt(20, 41)) {
            targetSpeedOffset = Random.nextDouble(-5.0, -1.0)
            ticksSinceLastChange = 0
        }

        // Target is speed limit minus 1-5 km/h
        val target = limitKmh.toDouble() + targetSpeedOffset
        val diff = target - speed

        // Full throttle until close to target - like a real driver
        throttle = when {
            diff > 3 -> 1.0       // Full throttle when far from target
            diff > 1 -> 0.6       // Ease off when getting close
            diff > 0.3 -> 0.3     // Gentle throttle to maintain
            else -> 0.0           // Coast when at speed
        }

        // Only brake if significantly over target
        brake = when {
            diff < -5 -> 0.7
            diff < -2 -> 0.4
            else -> 0.0
        }
    }

    private fun vehiclePhysicsTick() {
        val gearRatio = gearRatios[currentGear - 1]
        val wheelTorque = throttle * maxEngineTorque * gearRatio * finalDrive
        val driveForce = wheelTorque / wheelRadius

        // Reduced drag for faster acceleration
        val dragForce = 0.5 * 1.2 * 0.25 * (speed / 3.6).pow(2)
        val rollingResistance = 0.01 * vehicleMass * 9.81
        val brakeForce = brake * 10000

        val netForce = driveForce - dragForce - rollingResistance - brakeForce
        acceleration = netForce / vehicleMass

        // Much faster speed increase - realistic car acceleration
        speed += acceleration * 1.2
        speed = speed.coerceIn(0.0, 160.0)

        engineRpm = max(900.0, speed * gearRatio * finalDrive * 40)

        if (engineRpm > 6000 && currentGear < gearRatios.size) currentGear++
        if (engineRpm < 1500 && currentGear > 1) currentGear--
    }

    private fun scoringModel(limitKmh: Int) {
        val diff = abs(speed - limitKmh.toDouble())

        when {
            acceleration > 3.0 -> score -= 1.5
            acceleration < -4.0 -> score -= 2.0
            abs(acceleration) < 1.0 -> {
                score += 0.3
                simBonus += 2
            }
        }

        if (diff <= 5.0) simBonus += 1
        score = score.coerceIn(0.0, 100.0)
    }

    private fun publishTexts(limitKmh: Int) {
        _uiState.update {
            it.copy(
                speedText = "Speed: ${speed.toInt()} km/h",
                rpmText = "RPM: ${engineRpm.toInt()}",
                gearText = "Gear: $currentGear",
                scoreText = "Driver Score: ${score.toInt()}",
                simBonusText = "Bonus Points: $simBonus",
                modeText = "Mode: Speedlimit $limitKmh km/h"
            )
        }
    }

    private fun advanceAlongRoute(metersToMove: Double) {
        var remaining = metersToMove
        while (remaining > 0 && routeIndex < routeCoords.lastIndex) {
            val a = routeCoords[routeIndex]
            val b = routeCoords[routeIndex + 1]
            val dist = haversineMeters(a, b)
            if (dist <= 0.1) {
                routeIndex++
                continue
            }
            if (remaining >= dist) {
                remaining -= dist
                routeIndex++
            } else {
                remaining = 0.0
            }
        }
    }

    private fun parseMaxspeedKmh(raw: String?): Int? {
        if (raw.isNullOrBlank()) return null
        val v = raw.trim().lowercase()
        val digits = v.replace("km/h", "").trim()
        val n = digits.takeWhile { it.isDigit() }.toIntOrNull() ?: return null
        return if (v.contains("mph")) (n * 1.60934).roundToInt() else n
    }

    private fun haversineMeters(a: LatLon, b: LatLon): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(b.lat - a.lat)
        val dLon = Math.toRadians(b.lon - a.lon)
        val lat1 = Math.toRadians(a.lat)
        val lat2 = Math.toRadians(b.lat)

        val h = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        return 2 * r * asin(sqrt(h))
    }
}