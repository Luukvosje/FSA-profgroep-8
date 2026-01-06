package com.profgroep8.rmc_app.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.network.services.ApiResult
import com.example.network.services.UserServiceImpl
import com.profgroep8.rmc_app.ui.screens.bonuspoints.BonusPointsUIState
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.*

class BonusPointsViewModel : BaseViewModel() {

    // ===== backend user API (your own server) =====
    private val userService = object : UserServiceImpl() {}

    // ===== external APIs http client =====
    private val http = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    // ✅ Put your OpenRouteService key here
    // ORS free plan exists, limits apply :contentReference[oaicite:7]{index=7}
    private val ORS_API_KEY = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjljNmNlYjFhMTQ2NzQwNDk5ZTE1ODgwOTc0MzhmYzQyIiwiaCI6Im11cm11cjY0In0="

    private val _uiState = MutableStateFlow(BonusPointsUIState())
    val uiState: StateFlow<BonusPointsUIState> = _uiState.asStateFlow()

    // ===== jobs =====
    private var apiRefreshJob: Job? = null
    private var simJob: Job? = null

    // ===== logged-in user =====
    private var cachedUserId: Int? = null

    // ===== server points =====
    private var serverPoints: Int = 0

    // ===== simulation bonus (added) =====
    private var simBonus: Int = 0
    private var basePointsAtSimStart: Int = 0

    // ===== route geometry =====
    private var routeCoords: List<LatLon> = emptyList()
    private var routeIndex: Int = 0

    // ===== simulation physics (from your test project) =====
    private val vehicleMass = 1500.0
    private val wheelRadius = 0.32
    private val maxEngineTorque = 250.0
    private val gearRatios = listOf(3.6, 2.1, 1.4, 1.0, 0.8)
    private val finalDrive = 3.4

    private var currentGear = 1
    private var engineRpm = 900.0

    private var speed = 0.0          // km/h
    private var throttle = 0.0       // 0..1
    private var brake = 0.0          // 0..1
    private var acceleration = 0.0

    private var score = 100.0

    // ===== strategy pattern =====
    private interface RouteStrategy {
        suspend fun buildRoute(start: LatLon, end: LatLon): List<LatLon>
    }

    private interface SpeedLimitStrategy {
        suspend fun speedLimitKmhAt(point: LatLon): Int?
    }

    // ---- OpenRouteService route strategy (GeoJSON) ----
    private val orsRouteStrategy = object : RouteStrategy {
        override suspend fun buildRoute(start: LatLon, end: LatLon): List<LatLon> {
            if (ORS_API_KEY.startsWith("PUT_")) {
                throw IllegalStateException("Missing ORS_API_KEY. Put your OpenRouteService key in BonusPointsViewModel.")
            }

            // ORS supports GeoJSON format for route geometry :contentReference[oaicite:8]{index=8}
            val res: OrsGeoJsonResponse = http.post("https://api.openrouteservice.org/v2/directions/driving-car/geojson") {
                header("Authorization", ORS_API_KEY)
                contentType(ContentType.Application.Json)
                setBody(
                    OrsRouteRequest(
                        coordinates = listOf(
                            listOf(start.lon, start.lat),
                            listOf(end.lon, end.lat)
                        )
                    )
                )
            }.body()

            val coords = res.features.firstOrNull()?.geometry?.coordinates.orEmpty()
            // ORS returns [lon,lat] pairs inside coordinates
            return coords.mapNotNull { pair ->
                if (pair.size >= 2) LatLon(lat = pair[1], lon = pair[0]) else null
            }
        }
    }

    // ---- Overpass maxspeed strategy ----
    // OSM maxspeed tag defines legal limit :contentReference[oaicite:9]{index=9}
    private val overpassSpeedLimitStrategy = object : SpeedLimitStrategy {
        // cache to avoid hammering Overpass
        private var lastPoint: LatLon? = null
        private var lastLimit: Int? = null
        private var lastFetchTimeMs: Long = 0L

        override suspend fun speedLimitKmhAt(point: LatLon): Int? {
            val now = System.currentTimeMillis()
            // only query at most every 2 seconds
            if (lastPoint != null && now - lastFetchTimeMs < 2000) return lastLimit

            lastPoint = point
            lastFetchTimeMs = now

            // Query ways (and nodes) around current coordinate
            val query = """
                [out:json][timeout:25];
                (
                  way(around:25,${point.lat},${point.lon})["highway"]["maxspeed"];
                  node(around:25,${point.lat},${point.lon})["maxspeed"];
                );
                out tags 10;
            """.trimIndent()

            val overpass: OverpassResponse = http.post("https://overpass-api.de/api/interpreter") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(listOf("data" to query).formUrlEncode())
                header(HttpHeaders.UserAgent, "rmc-app/1.0 (student project)")
            }.body()

            val raw = overpass.elements
                .firstOrNull { it.tags?.maxspeed != null }
                ?.tags?.maxspeed

            val parsed = parseMaxspeedKmh(raw)
            lastLimit = parsed
            return parsed
        }
    }

    // chosen strategies
    private var routeStrategy: RouteStrategy = orsRouteStrategy
    private var speedLimitStrategy: SpeedLimitStrategy = overpassSpeedLimitStrategy

    init {
        startApiRefresh()
    }

    // ============================
    // UI events
    // ============================
    fun onStartAddressChanged(v: String) {
        _uiState.update { it.copy(startAddress = v) }
    }

    fun onEndAddressChanged(v: String) {
        _uiState.update { it.copy(endAddress = v) }
    }

    fun logout() {
        stopSimulationInternal(finalDbUpdate = false)
        userService.logout()
        cachedUserId = null
        _uiState.update { it.copy(isUnauthorized = true) }
    }

    // ============================
    // BONUSPOINTS API REFRESH (1s)
    // ============================
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
            val me = userService.getMe()
            if (me is ApiResult.Error) {
                _uiState.update { it.copy(isUnauthorized = true, isLoading = false) }
                return
            }
            val id = (me as ApiResult.Success).data.userID
            cachedUserId = id
            id
        }

        when (val pointsRes = userService.getBonusPoints(userId)) {
            is ApiResult.Success -> {
                serverPoints = pointsRes.data
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        bonusPoints = serverPoints,
                        errorMessage = null,
                        isUnauthorized = false
                    )
                }
            }
            is ApiResult.Error -> {
                _uiState.update { it.copy(errorMessage = pointsRes.exception.toString(), isLoading = false) }
            }
        }
    }

    // ============================
    // START SIMULATION (route-based)
    // ============================
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

                // Nominatim usage: be gentle and cache; public API is rate-limited :contentReference[oaicite:10]{index=10}
                val start = geocodeNominatim(startAddr)
                delay(1100) // important: respect rate limit
                val end = geocodeNominatim(endAddr)

                _uiState.update { it.copy(simulationStatus = "Routing...") }

                routeCoords = routeStrategy.buildRoute(start, end)
                if (routeCoords.size < 2) throw IllegalStateException("Route not found.")

                // Reset sim state
                resetSimulationState()
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

                // main loop: 100ms ticks (like your test project)
                while (true) {
                    if (!uiState.value.isSimulationRunning) break

                    val current = routeCoords[routeIndex]
                    val dest = routeCoords.last()

                    // look up speed limit (may be null)
                    val limit = speedLimitStrategy.speedLimitKmhAt(current) ?: 50
                    // driver model tries to hold exact speed limit
                    driverModelBySpeedLimit(limit)
                    vehiclePhysicsTick()

                    scoringModel(limit)
                    publishSimTexts(limit)

                    // move along route based on current speed
                    // distance to travel this tick:
                    val metersPerSec = speed / 3.6
                    val metersThisTick = metersPerSec * 0.1 // 100ms
                    advanceAlongRoute(metersThisTick)

                    // destination reached?
                    val remaining = haversineMeters(routeCoords[routeIndex], dest)
                    if (routeIndex >= routeCoords.lastIndex || remaining < 10.0) {
                        // stop simulation + final db update once
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
        // cancel loop job
        simJob?.cancel()
        simJob = null

        viewModelScope.launch {
            if (finalDbUpdate) {
                val userId = cachedUserId
                if (userId != null) {
                    val finalPoints = basePointsAtSimStart + simBonus
                    // final write to DB
                    userService.updateBonusPoints(userId, finalPoints)
                    // refresh UI from DB next tick anyway, but update local quickly
                    serverPoints = finalPoints
                    _uiState.update { it.copy(bonusPoints = finalPoints) }
                }
            }

            _uiState.update { it.copy(isSimulationRunning = false) }
        }
    }

    // ============================
    // SIMULATION helpers
    // ============================
    private fun resetSimulationState() {
        speed = 0.0
        engineRpm = 900.0
        currentGear = 1
        throttle = 0.0
        brake = 0.0
        acceleration = 0.0
        score = 100.0
    }

    private fun driverModelBySpeedLimit(limitKmh: Int) {
        val target = limitKmh.toDouble()

        // simple controller: accelerate if below, brake if above
        val diff = target - speed
        throttle = when {
            diff > 15 -> 0.7
            diff > 5 -> 0.4
            diff > 1 -> 0.2
            else -> 0.0
        }
        brake = when {
            diff < -10 -> 0.6
            diff < -3 -> 0.3
            else -> 0.0
        }
    }

    private fun vehiclePhysicsTick() {
        val gearRatio = gearRatios[currentGear - 1]
        val wheelTorque = throttle * maxEngineTorque * gearRatio * finalDrive
        val driveForce = wheelTorque / wheelRadius

        val dragForce = 0.5 * 1.2 * 0.32 * (speed / 3.6).pow(2)
        val rollingResistance = 0.015 * vehicleMass * 9.81
        val brakeForce = brake * 8000

        val netForce = driveForce - dragForce - rollingResistance - brakeForce
        acceleration = netForce / vehicleMass

        speed += acceleration * 0.36 // 100ms-ish tuning from your file
        speed = speed.coerceIn(0.0, 160.0)

        engineRpm = max(900.0, speed * gearRatio * finalDrive * 40)

        if (engineRpm > 6000 && currentGear < gearRatios.size) currentGear++
        if (engineRpm < 1500 && currentGear > 1) currentGear--
    }

    private fun scoringModel(limitKmh: Int) {
        // reward staying near speed limit and smooth driving
        val diff = abs(speed - limitKmh.toDouble())

        when {
            acceleration > 3.0 -> {
                score -= 1.5
            }
            acceleration < -4.0 -> {
                score -= 2.0
            }
            abs(acceleration) < 1.0 -> {
                score += 0.3
                simBonus += 2
            }
        }

        // extra: if within 3 km/h of limit, reward
        if (diff <= 3.0) simBonus += 1

        score = score.coerceIn(0.0, 100.0)

        // push to DB once per second through the already-running refresh loop
        // (we do final update on stop)
        if (simBonus % 10 == 0) {
            // nothing, avoid spamming
        }
    }

    private fun publishSimTexts(limitKmh: Int) {
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
                // we don't interpolate mid-segment for display; move index when reached
                // (simple + stable)
                remaining = 0.0
            }
        }
    }

    // ============================
    // External API: Nominatim geocode
    // ============================
    private suspend fun geocodeNominatim(query: String): LatLon {
        val url = "https://nominatim.openstreetmap.org/search"
        val res: List<NominatimItem> = http.get(url) {
            parameter("q", query)
            parameter("format", "json")
            parameter("limit", "1")
            header(HttpHeaders.UserAgent, "rmc-app/1.0 (student project)") // required-ish
        }.body()

        val item = res.firstOrNull() ?: throw IllegalStateException("Address not found: $query")
        return LatLon(lat = item.lat.toDouble(), lon = item.lon.toDouble())
    }

    // ============================
    // Utilities + models
    // ============================
    @Serializable
    private data class OrsRouteRequest(
        val coordinates: List<List<Double>>
    )

    @Serializable
    private data class OrsGeoJsonResponse(
        val features: List<OrsFeature> = emptyList()
    )

    @Serializable
    private data class OrsFeature(
        val geometry: OrsGeometry? = null
    )

    @Serializable
    private data class OrsGeometry(
        val coordinates: List<List<Double>> = emptyList()
    )

    @Serializable
    private data class NominatimItem(
        val lat: String,
        val lon: String
    )

    @Serializable
    private data class OverpassResponse(
        val elements: List<OverpassElement> = emptyList()
    )

    @Serializable
    private data class OverpassElement(
        val tags: OverpassTags? = null
    )

    @Serializable
    private data class OverpassTags(
        @SerialName("maxspeed") val maxspeed: String? = null
    )

    private data class LatLon(val lat: Double, val lon: Double)

    private fun parseMaxspeedKmh(raw: String?): Int? {
        if (raw.isNullOrBlank()) return null
        // examples: "50", "50 km/h", "80 mph", "signals", "walk"
        val digits = raw.trim().lowercase()
            .replace("km/h", "")
            .trim()

        val value = digits.takeWhile { it.isDigit() }
        val n = value.toIntOrNull() ?: return null

        return if (raw.lowercase().contains("mph")) {
            // mph -> km/h
            (n * 1.60934).roundToInt()
        } else n
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
