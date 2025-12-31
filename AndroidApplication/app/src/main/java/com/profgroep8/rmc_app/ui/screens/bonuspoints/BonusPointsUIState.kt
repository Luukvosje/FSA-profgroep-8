package com.profgroep8.rmc_app.ui.screens.bonuspoints

data class BonusPointsUIState(
    val isLoading: Boolean = true,
    val bonusPoints: Int? = null,
    val isUnauthorized: Boolean = false,
    val errorMessage: String? = null
)