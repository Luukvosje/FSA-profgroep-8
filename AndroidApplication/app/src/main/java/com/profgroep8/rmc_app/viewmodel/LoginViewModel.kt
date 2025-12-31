package com.profgroep8.rmc_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.models.remote.LoginUserDTO
import com.example.network.services.ApiResult
import com.example.network.services.UserServiceImpl
import com.profgroep8.rmc_app.ui.screens.login.LoginUIEvent
import com.profgroep8.rmc_app.ui.screens.login.LoginUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val userService = object : UserServiceImpl() {}

    private val _uiState = MutableStateFlow(LoginUIState())
    val uiState: StateFlow<LoginUIState> = _uiState.asStateFlow()

    fun onEvent(event: LoginUIEvent) {
        when (event) {

            is LoginUIEvent.EmailChanged ->
                _uiState.update { it.copy(email = event.email) }

            is LoginUIEvent.PasswordChanged ->
                _uiState.update { it.copy(password = event.password) }

            LoginUIEvent.LoginButtonClicked ->
                login()

            LoginUIEvent.ErrorShown ->
                _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun login() {
        val state = _uiState.value

        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Please enter email and password")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = userService.login(
                LoginUserDTO(
                    email = state.email,
                    password = state.password
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
                                ?: "Invalid email or password"
                        )
                    }
            }
        }
    }
}