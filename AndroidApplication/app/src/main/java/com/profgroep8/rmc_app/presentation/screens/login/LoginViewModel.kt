package com.profgroep8.rmc_app.presentation.screens.login

import com.profgroep8.rmc_app.presentation.screens.welcome.WelcomeUIState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUIState())
    val uiState: StateFlow<LoginUIState> = _uiState.asStateFlow()


    fun onEvent(event: LoginUIEvent) {

    }
}
