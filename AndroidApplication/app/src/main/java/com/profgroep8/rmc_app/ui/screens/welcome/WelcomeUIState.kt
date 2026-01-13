package com.profgroep8.rmc_app.ui.screens.welcome

data class WelcomeUIState(
    val isLoading: Boolean = false,
    val navigateToHome: Boolean = false,
    val hasCheckedLogin: Boolean = false
)