package com.profgroep8.rmc_app.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.network.services.ApiResult
import com.example.network.services.UserServiceImpl
import com.profgroep8.rmc_app.ui.screens.bonuspoints.BonusPointsUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BonusPointsViewModel : BaseViewModel() {

    private val userService = object : UserServiceImpl() {}

    private val _uiState = MutableStateFlow(BonusPointsUIState())
    val uiState: StateFlow<BonusPointsUIState> = _uiState.asStateFlow()

    init {
        loadBonusPoints()
    }

    private fun loadBonusPoints() {
        viewModelScope.launch {
            _uiState.value = BonusPointsUIState(isLoading = true)

            // 🔐 Check login via /users/me
            when (val meResult = userService.getMe()) {
                is ApiResult.Error -> {
                    _uiState.value = BonusPointsUIState(
                        isLoading = false,
                        isUnauthorized = true
                    )
                }

                is ApiResult.Success -> {
                    val userId = meResult.data.userID

                    when (val pointsResult = userService.getBonusPoints(userId)) {
                        is ApiResult.Success -> {
                            _uiState.value = BonusPointsUIState(
                                isLoading = false,
                                bonusPoints = pointsResult.data
                            )
                        }

                        is ApiResult.Error -> {
                            _uiState.value = BonusPointsUIState(
                                isLoading = false,
                                errorMessage = pointsResult.exception.message
                                    ?: "Failed to load bonus points"
                            )
                        }
                    }
                }
            }
        }
    }
}