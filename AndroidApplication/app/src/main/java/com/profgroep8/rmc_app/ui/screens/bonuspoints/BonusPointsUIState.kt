package com.profgroep8.rmc_app.ui.screens.bonuspoints

data class BonusPointsUIState(
    val isLoading: Boolean = false,
    val bonusPoints: Int = 0,
    val errorMessage: String? = null,
    val isUnauthorized: Boolean = false,
    val isSimulationRunning: Boolean = false
)