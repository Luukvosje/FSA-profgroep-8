package com.profgroep8.rmc_app.ui.screens.bonuspoints

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