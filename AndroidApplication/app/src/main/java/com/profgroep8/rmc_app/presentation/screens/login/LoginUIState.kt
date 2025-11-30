package com.profgroep8.rmc_app.presentation.screens.login

data class LoginUIState(
    var isLoading: Boolean = false,
    var email: String = "",
    var password: String = ""
)