package com.profgroep8.rmc_app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.interfaces.services.ServiceFactory
import com.profgroep8.rmc_app.data.TokenManager
import com.profgroep8.rmc_app.ui.screens.welcome.WelcomeUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WelcomeViewModel(
    private val tokenManager: TokenManager,
    private val serviceFactory: ServiceFactory
) : ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeUIState())
    val uiState: StateFlow<WelcomeUIState> = _uiState.asStateFlow()

    init {
        checkExistingLogin()
    }

    private fun checkExistingLogin() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val token = tokenManager.getToken()
            val hasLocalToken = token != null
            if (hasLocalToken) {
                serviceFactory.userService.logout()
                serviceFactory.userService.loginWithToken(token)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        navigateToHome = true,
                        hasCheckedLogin = true
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        navigateToHome = false,
                        hasCheckedLogin = true
                    )
                }
            }
        }
    }

    fun onNavigationHandled() {
        _uiState.update {
            it.copy(navigateToHome = false)
        }
    }
}