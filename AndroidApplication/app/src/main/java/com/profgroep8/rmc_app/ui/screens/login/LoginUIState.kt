package com.profgroep8.rmc_app.ui.screens.login

data class LoginUIState(
    val isLoading: Boolean = false,
    val email: String = "",
    val password: String = "",
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)