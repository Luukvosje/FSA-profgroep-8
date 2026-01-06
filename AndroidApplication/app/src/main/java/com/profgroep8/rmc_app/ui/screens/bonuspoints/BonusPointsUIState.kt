package com.profgroep8.rmc_app.ui.screens.bonuspoints

data class BonusPointsUIState(
    val isLoading: Boolean = false,

    // ✅ Always the DB value (refreshed every second)
    val bonusPoints: Int = 0,

    val errorMessage: String? = null,
    val isUnauthorized: Boolean = false,

    // Simulation
    val isSimulationRunning: Boolean = false,
    val simulationStatus: String = "",

    // User input
    val startAddress: String = "",
    val endAddress: String = "",

    // Simulation display (same labels as your test project)
    val speedText: String = "Speed: 0 km/h",
    val rpmText: String = "RPM: 900",
    val gearText: String = "Gear: 1",
    val scoreText: String = "Driver Score: 100",
    val simBonusText: String = "Bonus Points: 0",
    val modeText: String = "Mode: Speedlimit"
)