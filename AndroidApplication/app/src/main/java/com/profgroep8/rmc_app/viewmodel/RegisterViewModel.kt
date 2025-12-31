package com.profgroep8.rmc_app.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.models.remote.CreateUserDTO
import com.example.network.services.ApiResult
import com.example.network.services.UserServiceImpl
import com.profgroep8.rmc_app.ui.screens.register.RegisterUIEvent
import com.profgroep8.rmc_app.ui.screens.register.RegisterUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val userService = object : UserServiceImpl() {}

    private val _uiState = MutableStateFlow(RegisterUIState())
    val uiState: StateFlow<RegisterUIState> = _uiState.asStateFlow()

    fun onEvent(event: RegisterUIEvent) {
        when (event) {

            is RegisterUIEvent.FullNameChanged ->
                _uiState.update { it.copy(fullName = event.value) }

            is RegisterUIEvent.EmailChanged ->
                _uiState.update { it.copy(email = event.value) }

            is RegisterUIEvent.PasswordChanged ->
                _uiState.update { it.copy(password = event.value) }

            is RegisterUIEvent.PhoneChanged ->
                _uiState.update { it.copy(phone = event.value) }

            is RegisterUIEvent.AddressChanged ->
                _uiState.update { it.copy(address = event.value) }

            is RegisterUIEvent.ZipcodeChanged ->
                _uiState.update { it.copy(zipcode = event.value) }

            is RegisterUIEvent.CityChanged ->
                _uiState.update { it.copy(city = event.value) }

            is RegisterUIEvent.CountryISOChanged ->
                _uiState.update { it.copy(countryISO = event.value) }

            RegisterUIEvent.RegisterButtonClicked ->
                register()

            RegisterUIEvent.ErrorShown ->
                _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun register() {
        val state = _uiState.value

        if (
            state.fullName.isBlank() ||
            state.email.isBlank() ||
            state.password.isBlank() ||
            state.phone.isBlank() ||
            state.address.isBlank() ||
            state.zipcode.isBlank() ||
            state.city.isBlank() ||
            state.countryISO.isBlank()
        ) {
            _uiState.update { it.copy(errorMessage = "Please fill in all fields") }
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.update { it.copy(errorMessage = "Invalid email address") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = userService.register(
                CreateUserDTO(
                    fullName = state.fullName,
                    email = state.email,
                    password = state.password,
                    phone = state.phone,
                    address = state.address,
                    zipcode = state.zipcode,
                    city = state.city,
                    countryISO = state.countryISO
                )
            )

            when (result) {
                is ApiResult.Success ->
                    _uiState.update {
                        it.copy(isLoading = false, isSuccess = true)
                    }

                is ApiResult.Error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.message
                                ?: "Registration failed"
                        )
                    }
            }
        }
    }
}