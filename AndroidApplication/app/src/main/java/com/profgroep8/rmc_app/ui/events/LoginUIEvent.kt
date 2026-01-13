package com.profgroep8.rmc_app.ui.events

sealed interface LoginUIEvent {
    data class EmailChanged(val email: String) : LoginUIEvent
    data class PasswordChanged(val password: String) : LoginUIEvent
    object LoginButtonClicked : LoginUIEvent
    object ErrorShown : LoginUIEvent
}
