package com.profgroep8.rmc_app.ui.screens.register

data class RegisterUIState(
    val isLoading: Boolean = false,

    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val phone: String = "",
    val address: String = "",
    val zipcode: String = "",
    val city: String = "",
    val countryISO: String = "",

    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)