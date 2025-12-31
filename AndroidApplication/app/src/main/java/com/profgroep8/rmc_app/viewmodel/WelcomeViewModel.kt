package com.profgroep8.rmc_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profgroep8.rmc_app.ui.screens.welcome.WelcomeUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WelcomeViewModel(
//    private val repository: AuthRepository
) : ViewModel() {

//    var loginState by mutableStateOf<String>("")

    private val _uiState = MutableStateFlow(WelcomeUIState())
    val uiState: StateFlow<WelcomeUIState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
//            try {
//                val response = repository.login(email, password)
//                loginState = "Success: ${response.token}"
//            } catch (e: Exception) {
//                loginState = "Error: ${e.message}"
//            }
        }
    }
}