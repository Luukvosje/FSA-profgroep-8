package com.profgroep8.rmc_app.viewmodel.user

import androidx.lifecycle.viewModelScope
import com.example.network.interfaces.services.ServiceFactory
import com.example.network.services.ApiResult
import com.profgroep8.rmc_app.viewmodel.BaseViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

private const val ORS_API_KEY: String = ""

private data class LatLon(val lat: Double, val lon: Double)
private data class RoadSegment(
    val startIndex: Int,
    val endIndex: Int,
    val roadType: String,
    val speedLimit: Int
)

data class BonusPointsUIState(
    val bonusPoints: Int = 0,
    val isUnauthorized: Boolean = false,
    val isLoading: Boolean = false,
    val isSimulationRunning: Boolean = false,
    val errorMessage: String? = null,
    val simulationStatus: String = "",
    val startAddress: String = "",
    val endAddress: String = "",
    val speedText: String = "",
    val rpmText: String = "",
    val gearText: String = "",
    val scoreText: String = "",
    val simBonusText: String = "",
    val modeText: String = "",
    val performanceGraph: List<Float> = List(10) { 50f }
)

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
    private var totalRouteDistance: Double = 0.0
    private var distanceTraveled: Double = 0.0

    private var roadSegments = mutableListOf<RoadSegment>()
    private var currentSpeedLimit = 50
    private var currentRoadType = "unclassified"

    private val vehicleMass = 1200.0
    private val wheelRadius = 0.32
    private val maxEngineTorque = 500.0
    private val gearRatios = listOf(3.6, 2.1, 1.4, 1.0, 0.8)
    private val finalDrive = 3.4

    private var currentGear = 1
    private var engineRpm = 900.0
    private var speed = 0.0
    private var throttle = 0.0
    private var brake = 0.0
    private var acceleration = 0.0
    private var score = 100.0

    private var targetSpeedOffset = 0.0
    private var ticksSinceLastChange = 0
    private var driverSkillFactor = Random.nextDouble(0.5, 0.85) // Most drivers are average
    private var driverMistakeChance = Random.nextDouble(0.15, 0.35) // 15-35% chance of mistakes

    private var performanceHistory = mutableListOf<Float>()
    private var tickCounter = 0
    private var tickScoreAccumulator = 0.0
    private var tickCount = 0

    init {
        startApiRefresh()
    }

    fun onStartAddressChanged(v: String) = _uiState.update { it.copy(startAddress = v) }
    fun onEndAddressChanged(v: String) = _uiState.update { it.copy(endAddress = v) }

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
                delay(1100)
                val end = geocodeNominatim(endAddr)

                _uiState.update { it.copy(simulationStatus = "Routing...") }

                routeCoords = fetchRouteWithFallback(start, end)
                if (routeCoords.size < 2) throw IllegalStateException("Route not found.")

                totalRouteDistance = calculateTotalDistance(routeCoords)

                _uiState.update { it.copy(simulationStatus = "Generating realistic route...") }
                generateRealisticRoute()

                resetSim()
                routeIndex = 0
                distanceTraveled = 0.0
                basePointsAtSimStart = serverPoints
                simBonus = 0
                updateCurrentRoad()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSimulationRunning = true,
                        simulationStatus = "Driving ${String.format("%.1f", totalRouteDistance / 1000)} km route..."
                    )
                }

                while (true) {
                    if (!uiState.value.isSimulationRunning) break

                    updateCurrentRoad()

                    driverModelBySpeedLimit(currentSpeedLimit)
                    vehiclePhysicsTick()
                    scoringModel(currentSpeedLimit)
                    publishTexts(currentSpeedLimit)

                    val metersPerSec = speed / 3.6
                    val metersThisTick = metersPerSec * 0.1
                    val actualMoved = advanceAlongRoute(metersThisTick)
                    distanceTraveled += actualMoved

                    // Check if arrived at destination
                    if (routeIndex >= routeCoords.lastIndex) {
                        stopSimulationInternal(finalDbUpdate = true)
                        _uiState.update {
                            it.copy(
                                simulationStatus = "Arrived ✅ Total: ${String.format("%.1f", distanceTraveled / 1000)} km"
                            )
                        }
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
    private suspend fun generateRealisticRoute() {
        roadSegments.clear()

        val samplePoints = listOf(0, routeCoords.size / 4, routeCoords.size / 2, (routeCoords.size * 3) / 4)
        val detectedRoads = mutableMapOf<Int, Pair<String, Int>>()

        for (idx in samplePoints) {
            if (idx < routeCoords.size) {
                val detection = detectRoadTypeWithTimeout(routeCoords[idx])
                if (detection != null) {
                    detectedRoads[idx] = detection
                }
                delay(500)
            }
        }

        val routePhases = listOf(
            RoutePhase(0.05, 0.10, listOf("residential", "living_street", "tertiary")),
            RoutePhase(0.10, 0.20, listOf("tertiary", "secondary", "primary")),
            RoutePhase(0.10, 0.15, listOf("primary", "trunk", "motorway_link")),
            RoutePhase(0.40, 0.50, listOf("motorway", "trunk")),
            RoutePhase(0.10, 0.15, listOf("motorway_link", "trunk", "primary")),
            RoutePhase(0.10, 0.15, listOf("primary", "secondary", "tertiary")),
            RoutePhase(0.05, 0.10, listOf("tertiary", "residential", "living_street"))
        )

        var currentIndex = 0

        for (phase in routePhases) {
            val phaseLength = Random.nextDouble(phase.minPercent, phase.maxPercent)
            val phaseSegmentCount = Random.nextInt(1, 4) // 1-3 segments per phase
            val segmentLength = ((routeCoords.size * phaseLength) / phaseSegmentCount).toInt()

            repeat(phaseSegmentCount) {
                if (currentIndex >= routeCoords.size) return@repeat

                val roadType = phase.roadTypes.random()
                val speedLimit = getDefaultSpeedForRoadType(roadType)

                val endIndex = min(currentIndex + segmentLength, routeCoords.size - 1)

                roadSegments.add(RoadSegment(
                    startIndex = currentIndex,
                    endIndex = endIndex,
                    roadType = roadType,
                    speedLimit = speedLimit
                ))

                currentIndex = endIndex + 1
            }
        }

        if (currentIndex < routeCoords.size - 1) {
            roadSegments.add(RoadSegment(
                startIndex = currentIndex,
                endIndex = routeCoords.size - 1,
                roadType = "residential",
                speedLimit = 30
            ))
        }
    }

    private data class RoutePhase(
        val minPercent: Double,
        val maxPercent: Double,
        val roadTypes: List<String>
    )

    private suspend fun detectRoadTypeWithTimeout(point: LatLon): Pair<String, Int>? {
        return withTimeoutOrNull(2000) {
            try {
                val query = "[out:json];way(around:100,${point.lat},${point.lon})[highway];out tags 1;"

                val text = http.post("https://overpass-api.de/api/interpreter") {
                    contentType(ContentType.Text.Plain)
                    setBody(query)
                    header(HttpHeaders.UserAgent, "rmc-app/1.0 (student project)")
                }.bodyAsText()

                val root = json.parseToJsonElement(text).jsonObject
                val elements = root["elements"]?.jsonArray ?: return@withTimeoutOrNull null

                val way = elements.firstOrNull()?.jsonObject ?: return@withTimeoutOrNull null
                val tags = way["tags"]?.jsonObject ?: return@withTimeoutOrNull null

                val highwayType = tags["highway"]?.jsonPrimitive?.contentOrNull ?: return@withTimeoutOrNull null
                val maxspeed = tags["maxspeed"]?.jsonPrimitive?.contentOrNull?.let { parseMaxspeedKmh(it) }
                    ?: getDefaultSpeedForRoadType(highwayType)

                Pair(highwayType, maxspeed)
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun getDefaultSpeedForRoadType(roadType: String): Int {
        return when (roadType) {
            "motorway" -> Random.nextInt(120, 131)
            "motorway_link" -> 80
            "trunk" -> Random.nextInt(90, 101)
            "trunk_link" -> 70
            "primary" -> Random.nextInt(70, 81)
            "primary_link" -> 60
            "secondary" -> Random.nextInt(50, 61)
            "tertiary" -> 50
            "residential" -> 30
            "living_street" -> 15
            else -> 50
        }
    }

    private fun updateCurrentRoad() {
        val segment = roadSegments.firstOrNull {
            routeIndex >= it.startIndex && routeIndex <= it.endIndex
        }

        if (segment != null) {
            currentSpeedLimit = segment.speedLimit
            currentRoadType = segment.roadType
        }
    }

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

    private suspend fun fetchRouteWithFallback(start: LatLon, end: LatLon): List<LatLon> {
        val hasOrsKey = ORS_API_KEY.isNotBlank()

        if (hasOrsKey) {
            try {
                return fetchRouteORS(start, end)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(simulationStatus = "ORS blocked. Using free OSRM routing...")
                }
            }
        }

        return fetchRouteOSRM(start, end)
    }

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

    private fun resetSim() {
        speed = 0.0
        engineRpm = 900.0
        currentGear = 1
        throttle = 0.0
        brake = 0.0
        acceleration = 0.0
        score = Random.nextDouble(70.0, 85.0)

        targetSpeedOffset = Random.nextDouble(-8.0, 2.0)
        ticksSinceLastChange = 0
        driverSkillFactor = Random.nextDouble(0.65, 0.9)
        driverMistakeChance = Random.nextDouble(0.1, 0.25)

        performanceHistory.clear()
        performanceHistory.addAll(List(100) { 0f })
        tickCounter = 0
        tickScoreAccumulator = 0.0
        tickCount = 0
    }

    private fun driverModelBySpeedLimit(limitKmh: Int) {
        ticksSinceLastChange++
        if (ticksSinceLastChange > Random.nextInt(15, 35)) {
            val mistake = if (Random.nextDouble() < driverMistakeChance) {
                Random.nextDouble(-15.0, 10.0)
            } else {
                Random.nextDouble(-8.0, 5.0) * driverSkillFactor
            }
            targetSpeedOffset = mistake
            ticksSinceLastChange = 0
        }

        val target = limitKmh.toDouble() + targetSpeedOffset
        val diff = target - speed

        val imperfection = Random.nextDouble(-0.3, 0.3)

        throttle = when {
            diff > 10 -> (1.0 + imperfection).coerceIn(0.0, 1.0)
            diff > 5 -> (0.8 + imperfection).coerceIn(0.0, 1.0)
            diff > 2 -> (0.6 + imperfection).coerceIn(0.0, 1.0)
            diff > 0.5 -> (0.3 + imperfection).coerceIn(0.0, 1.0)
            else -> 0.0
        }

        brake = when {
            diff < -15 -> (0.9 + imperfection).coerceIn(0.0, 1.0)
            diff < -8 -> (0.7 + imperfection).coerceIn(0.0, 1.0)
            diff < -4 -> (0.5 + imperfection).coerceIn(0.0, 1.0)
            diff < -1 -> (0.2 + imperfection).coerceIn(0.0, 1.0)
            else -> 0.0
        }
    }

    private fun vehiclePhysicsTick() {
        val gearRatio = gearRatios[currentGear - 1]
        val wheelTorque = throttle * maxEngineTorque * gearRatio * finalDrive
        val driveForce = wheelTorque / wheelRadius

        val dragForce = 0.5 * 1.2 * 0.25 * (speed / 3.6).pow(2)
        val rollingResistance = 0.01 * vehicleMass * 9.81
        val brakeForce = brake * 10000

        val netForce = driveForce - dragForce - rollingResistance - brakeForce
        acceleration = netForce / vehicleMass

        speed += acceleration * 1.2
        speed = speed.coerceIn(0.0, 160.0)

        engineRpm = max(900.0, speed * gearRatio * finalDrive * 40)

        if (engineRpm > 6000 && currentGear < gearRatios.size) currentGear++
        if (engineRpm < 1500 && currentGear > 1) currentGear--
    }

    private fun scoringModel(limitKmh: Int) {
        val diff = speed - limitKmh.toDouble()

        var instantScore = Random.nextDouble(70.0, 85.0)

        // Harsh acceleration penalty
        when {
            acceleration > 5.0 -> {
                score -= 3.0
                instantScore -= 40.0
            }
            acceleration > 3.5 -> {
                score -= 1.5
                instantScore -= 25.0
            }
            acceleration > 2.0 -> {
                score -= 0.8
                instantScore -= 10.0
            }
            abs(acceleration) < 0.8 -> {
                score += 0.5
                simBonus += 2
                instantScore += 15.0
            }
        }

        when {
            acceleration < -6.0 -> {
                score -= 4.0
                instantScore -= 50.0
            }
            acceleration < -4.0 -> {
                score -= 2.0
                instantScore -= 30.0
            }
            acceleration < -2.5 -> {
                score -= 1.0
                instantScore -= 15.0
            }
        }

        when {
            diff > 20 -> {
                score -= 4.0
                instantScore -= 60.0
            }
            diff > 12 -> {
                score -= 2.5
                instantScore -= 40.0
            }
            diff > 7 -> {
                score -= 1.5
                instantScore -= 25.0
            }
            diff > 3 -> {
                score -= 0.8
                instantScore -= 10.0
            }
            diff >= -2 && diff <= 2 -> {
                simBonus += 1
                instantScore += 10.0
            }
            diff < -12 -> {
                score -= 1.5
                instantScore -= 20.0
            }
        }

        if (Random.nextDouble() < 0.03) {
            score -= Random.nextDouble(1.0, 5.0)
            instantScore -= Random.nextDouble(10.0, 30.0)
        }

        instantScore = instantScore.coerceIn(0.0, 100.0)

        tickScoreAccumulator += instantScore
        tickCount++
        tickCounter++

        if (tickCounter >= 10) {
            val averageScore = (tickScoreAccumulator / tickCount).toFloat()

            performanceHistory.removeAt(0)
            performanceHistory.add(averageScore)

            // Reset counters
            tickCounter = 0
            tickScoreAccumulator = 0.0
            tickCount = 0
        }

        score = score.coerceIn(50.0, 100.0)

        if (score > 90) score -= 0.3
        if (score < 65) score += 0.3
    }

    private fun publishTexts(limitKmh: Int) {
        val progress = if (totalRouteDistance > 0) (distanceTraveled / totalRouteDistance * 100).toInt() else 0
        val remainingKm = if (totalRouteDistance > 0) {
            ((totalRouteDistance - distanceTraveled) / 1000).coerceAtLeast(0.0)
        } else 0.0

        _uiState.update {
            it.copy(
                speedText = "Speed: ${speed.toInt()} km/h",
                rpmText = "RPM: ${engineRpm.toInt()}",
                gearText = "Gear: $currentGear",
                scoreText = "Driver Score: ${score.toInt()}",
                simBonusText = "Bonus Points: $simBonus",
                modeText = "$currentRoadType: $limitKmh km/h | ${String.format("%.1f", remainingKm)} km remaining",
                performanceGraph = performanceHistory.toList()
            )
        }
    }

    private fun advanceAlongRoute(metersToMove: Double): Double {
        var remaining = metersToMove
        var totalMoved = 0.0

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
                totalMoved += dist
                routeIndex++
            } else {
                totalMoved += remaining
                remaining = 0.0
            }
        }

        return totalMoved
    }

    private fun calculateTotalDistance(coords: List<LatLon>): Double {
        var total = 0.0
        for (i in 0 until coords.size - 1) {
            total += haversineMeters(coords[i], coords[i + 1])
        }
        return total
    }

    private fun parseMaxspeedKmh(raw: String?): Int? {
        if (raw.isNullOrBlank()) return null
        val v = raw.trim().lowercase()
        val digits = v.replace("km/h", "").replace("mph", "").trim()
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