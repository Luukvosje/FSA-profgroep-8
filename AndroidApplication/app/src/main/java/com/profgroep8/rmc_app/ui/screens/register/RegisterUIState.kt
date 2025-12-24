package com.profgroep8.rmc_app.ui.screens.register

data class RegisterUIState(
    var isLoading: Boolean = false,
    var fullName: String = "",
    var email: String = "",
    var password: String = "",
    var phone: String = "",
    var address: String = "",
    var zipcode: String = "",
    var city: String = "",
    var countryISO: String = "",
    var points: String = "0"
)