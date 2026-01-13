package com.profgroep8.rmc_app.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.interfaces.services.ServiceFactory
import com.example.network.models.domain.User
import com.example.network.services.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserInfoUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class UserInfoViewModel(
    private val serviceFactory: ServiceFactory
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserInfoUiState())
    val uiState: StateFlow<UserInfoUiState> = _uiState.asStateFlow()

    init {
        loadUserInfo()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = serviceFactory.userService.getMe()) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = result.data,
                            error = null
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception.message
                        )
                    }
                }
            }
        }
    }

    fun refreshUserInfo() {
        loadUserInfo()
    }
}