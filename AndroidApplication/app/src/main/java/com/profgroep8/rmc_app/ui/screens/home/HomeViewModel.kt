package com.profgroep8.rmc_app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.services.ApiResult
import com.example.network.services.UserServiceImpl
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

class HomeViewModel : ViewModel() {

    private val userService = object : UserServiceImpl() {}

    private val _uiState = MutableStateFlow(HomeUIState())
    val uiState: StateFlow<HomeUIState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = userService.getMe()) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userName = result.data.fullName
                        )
                    }
                }

                is ApiResult.Error -> {
                    // Token invalid or expired → logout
                    logout()
                }
            }
        }
    }

    fun logout() {
        userService.logout()
        _uiState.update { it.copy(isLoggedOut = true) }
    }
}
