package com.profgroep8.rmc_app.viewmodel

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

data class HomeUIState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val isLoggedOut: Boolean = false
)

class HomeViewModel(
    private val serviceFactory: ServiceFactory,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUIState())
    val uiState: StateFlow<HomeUIState> = _uiState.asStateFlow()

    init {
        restoreUser()
        loadUser()
    }

    private fun restoreUser() {
        val email = tokenManager.getUserEmail()
        if (email != null) {
            _uiState.update {
                it.copy(userName = email.substringBefore("@"))
            }
        }
    }

    private fun loadUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = serviceFactory.userService.getMe()) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userName = result.data.fullName
                        )
                    }
                }

                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun logout() {
        tokenManager.clearSession()
        serviceFactory.userService.logout()
        _uiState.update { it.copy(isLoggedOut = true, userName = "") }
    }

    fun onLogoutHandled() {
        _uiState.update { it.copy(isLoggedOut = false) }
    }
}