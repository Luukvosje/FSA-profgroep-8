package com.profgroep8.rmc_app.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.interfaces.services.ServiceFactory
import com.example.network.services.ApiResult
import com.profgroep8.rmc_app.data.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class WelcomeUIState(
    val isLoading: Boolean = false,
    val navigateToHome: Boolean = false,
    val hasCheckedLogin: Boolean = false
)

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

            if (token != null && tokenManager.hasValidSession()) {
                val result = serviceFactory.userService.restoreSession(token)

                when (result) {
                    is ApiResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                navigateToHome = true,
                                hasCheckedLogin = true
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        tokenManager.clearSession()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                navigateToHome = false,
                                hasCheckedLogin = true
                            )
                        }
                    }
                }
            } else {
                tokenManager.clearSession()

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