package com.profgroep8.rmc_app.ui.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUIState())
    val uiState: StateFlow<RegisterUIState> = _uiState.asStateFlow()

    fun onEvent(event: RegisterUIEvent) {
        when(event) {
            is RegisterUIEvent.FullNameChanged -> {
                _uiState.update { it.copy(fullName = event.value) }
            }
            is RegisterUIEvent.EmailChanged -> {
                _uiState.update { it.copy(email = event.value) }
            }
            is RegisterUIEvent.PasswordChanged -> {
                _uiState.update { it.copy(password = event.value) }
            }
            is RegisterUIEvent.PhoneChanged -> {
                _uiState.update { it.copy(phone = event.value) }
            }
            is RegisterUIEvent.AddressChanged -> {
                _uiState.update { it.copy(address = event.value) }
            }
            is RegisterUIEvent.ZipcodeChanged -> {
                _uiState.update { it.copy(zipcode = event.value) }
            }
            is RegisterUIEvent.CityChanged -> {
                _uiState.update { it.copy(city = event.value) }
            }
            is RegisterUIEvent.CountryISOChanged -> {
                _uiState.update { it.copy(countryISO = event.value) }
            }
            is RegisterUIEvent.PointsChanged -> {
                _uiState.update { it.copy(points = event.value) }
            }
            is RegisterUIEvent.RegisterButtonClicked -> {

            }
        }
    }
}