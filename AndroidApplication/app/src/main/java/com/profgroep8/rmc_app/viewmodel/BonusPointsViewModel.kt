package com.profgroep8.rmc_app.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.network.services.ApiResult
import com.example.network.services.UserServiceImpl
import com.profgroep8.rmc_app.ui.screens.bonuspoints.BonusPointsUIState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.random.Random

class BonusPointsViewModel : BaseViewModel() {

    private val userService = object : UserServiceImpl() {}

    private val _uiState = MutableStateFlow(BonusPointsUIState())
    val uiState: StateFlow<BonusPointsUIState> = _uiState.asStateFlow()

    private val _simSpeedText = MutableStateFlow("Speed: 0 km/h")
    val simSpeedText: StateFlow<String> = _simSpeedText.asStateFlow()

    private val _simRpmText = MutableStateFlow("RPM: 900")
    val simRpmText: StateFlow<String> = _simRpmText.asStateFlow()

    private val _simGearText = MutableStateFlow("Gear: 1")
    val simGearText: StateFlow<String> = _simGearText.asStateFlow()

    private val _simScoreText = MutableStateFlow("Driver Score: 100")
    val simScoreText: StateFlow<String> = _simScoreText.asStateFlow()

    private val _simBonusText = MutableStateFlow("Bonus Points: 0")
    val simBonusText: StateFlow<String> = _simBonusText.asStateFlow()

    private val _simModeText = MutableStateFlow("Mode: City 🏙")
    val simModeText: StateFlow<String> = _simModeText.asStateFlow()

    private var apiJob: Job? = null
    private var simJob: Job? = null

    private var cachedUserId: Int? = null

    private var serverPoints: Int = 0

    private var simBonus: Int = 0
    private var basePointsAtSimStart: Int = 0

    private val vehicleMass = 1500.0
    private val wheelRadius = 0.32
    private val maxEngineTorque = 250.0
    private val gearRatios = listOf(3.6, 2.1, 1.4, 1.0, 0.8)
    private val finalDrive = 3.4

    private var currentGear = 1
    private var engineRpm = 900.0

    private var speed = 0.0
    private var throttle = 0.0
    private var brake = 0.0
    private var acceleration = 0.0

    private var cityMode = true
    private val cityLimit = 50.0
    private val highwayLimit = 100.0

    private var score = 0.0
    private var harshAccel = 0
    private var harshBrake = 0

    private interface DrivingStrategy {
        fun environmentModel()
        fun driverModel()
        fun vehiclePhysics()
        fun scoringModel()
    }

    private val realCarStrategy = object : DrivingStrategy {
        override fun environmentModel() {
            if (Random.nextDouble() < 0.002) cityMode = !cityMode
        }

        override fun driverModel() {
            val speedLimit = if (cityMode) cityLimit else highwayLimit

            throttle = when {
                speed < speedLimit - 10 -> 0.6
                speed < speedLimit -> 0.3
                else -> 0.0
            }

            brake = if (speed > speedLimit + 5) 0.5 else 0.0
        }

        override fun vehiclePhysics() {
            val gearRatio = gearRatios[currentGear - 1]
            val wheelTorque = throttle * maxEngineTorque * gearRatio * finalDrive
            val driveForce = wheelTorque / wheelRadius

            val dragForce = 0.5 * 1.2 * 0.32 * (speed / 3.6).pow(2)
            val rollingResistance = 0.015 * vehicleMass * 9.81
            val brakeForce = brake * 8000

            val netForce = driveForce - dragForce - rollingResistance - brakeForce
            acceleration = netForce / vehicleMass

            speed += acceleration * 0.36
            speed = speed.coerceIn(0.0, 160.0)

            engineRpm = max(900.0, speed * gearRatio * finalDrive * 40)

            if (engineRpm > 6000 && currentGear < gearRatios.size) currentGear++
            if (engineRpm < 1500 && currentGear > 1) currentGear--
        }

        override fun scoringModel() {
            when {
                acceleration > 3.0 -> {
                    harshAccel++
                    score -= 1.5
                }
                acceleration < -4.0 -> {
                    harshBrake++
                    score -= 2.0
                }
                abs(acceleration) < 1.0 -> {
                    score += 0.3
                    simBonus += 2
                }
            }

            if (speed in 40.0..90.0) simBonus += 1
            score = score.coerceIn(0.0, 100.0)
        }
    }

    private var drivingStrategy: DrivingStrategy = realCarStrategy

    init {
        startApiRefresh()
    }

    private fun startApiRefresh() {
        if (apiJob != null) return

        apiJob = viewModelScope.launch {
            // immediate
            refreshFromServerAndMaybeUpdate()

            // loop
            while (true) {
                delay(1000)
                refreshFromServerAndMaybeUpdate()
            }
        }
    }

    private fun refreshFromServerAndMaybeUpdate() {
        // 1) Ensure we have userId (/me)
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

        // 2) Fetch server points
        when (val pointsRes = userService.getBonusPoints(userId)) {
            is ApiResult.Error -> {
                _uiState.update { it.copy(errorMessage = pointsRes.exception.toString(), isLoading = false) }
                return
            }
            is ApiResult.Success -> {
                serverPoints = pointsRes.data
            }
        }

        if (_uiState.value.isSimulationRunning) {
            val desired = basePointsAtSimStart + simBonus
            if (desired != serverPoints) {
                // push update to DB
                val updateRes = userService.updateBonusPoints(userId, desired)
                if (updateRes is ApiResult.Success) {
                    serverPoints = desired
                } else if (updateRes is ApiResult.Error) {
                    _uiState.update { it.copy(errorMessage = updateRes.exception.toString()) }
                }
            }
        }

        // 4) Update UI: show DB points (always!)
        _uiState.update {
            it.copy(
                isLoading = false,
                bonusPoints = serverPoints,
                errorMessage = null,
                isUnauthorized = false
            )
        }
    }

    fun startSimulation() {
        if (simJob != null) return

        // reset like test project
        speed = 0.0
        engineRpm = 900.0
        currentGear = 1
        score = 0.0
        simBonus = 0
        harshAccel = 0
        harshBrake = 0
        cityMode = true

        // base points from DB at start
        basePointsAtSimStart = serverPoints

        _uiState.update { it.copy(isSimulationRunning = true) }
        publishSimTexts()

        simJob = viewModelScope.launch {
            while (true) {
                drivingStrategy.environmentModel()
                drivingStrategy.driverModel()
                drivingStrategy.vehiclePhysics()
                drivingStrategy.scoringModel()
                publishSimTexts()

                if (simBonus >= 3000) {
                    stopSimulation()
                    return@launch
                }

                delay(100) // same tick as test project
            }
        }
    }

    fun stopSimulation() {
        simJob?.cancel()
        simJob = null
        _uiState.update { it.copy(isSimulationRunning = false) }
    }

    fun logout() {
        stopSimulation()
        userService.logout()
        cachedUserId = null
        _uiState.update { it.copy(isUnauthorized = true) }
    }

    private fun publishSimTexts() {
        _simSpeedText.value = "Speed: ${speed.toInt()} km/h"
        _simRpmText.value = "RPM: ${engineRpm.toInt()}"
        _simGearText.value = "Gear: $currentGear"
        _simScoreText.value = "Driver Score: ${score.toInt()}"
        _simBonusText.value = "Bonus Points: $simBonus"
        _simModeText.value = if (cityMode) "Mode: City 🏙" else "Mode: Highway 🛣"
    }
}