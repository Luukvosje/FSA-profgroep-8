package com.profgroep8.rmc_app.presentation.screens.welcome

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class WelcomeViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    var loginState by mutableStateOf<String>("")
        private set

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = repository.login(email, password)
                loginState = "Success: ${response.token}"
            } catch (e: Exception) {
                loginState = "Error: ${e.message}"
            }
        }
    }
}
