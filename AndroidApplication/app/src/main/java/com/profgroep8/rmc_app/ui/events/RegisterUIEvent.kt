package com.profgroep8.rmc_app.ui.events

sealed interface RegisterUIEvent {
    data class FullNameChanged(val value: String) : RegisterUIEvent
    data class EmailChanged(val value: String) : RegisterUIEvent
    data class PasswordChanged(val value: String) : RegisterUIEvent
    data class PhoneChanged(val value: String) : RegisterUIEvent
    data class AddressChanged(val value: String) : RegisterUIEvent
    data class ZipcodeChanged(val value: String) : RegisterUIEvent
    data class CityChanged(val value: String) : RegisterUIEvent
    data class CountryISOChanged(val value: String) : RegisterUIEvent

    object RegisterButtonClicked : RegisterUIEvent
    object ErrorShown : RegisterUIEvent
}