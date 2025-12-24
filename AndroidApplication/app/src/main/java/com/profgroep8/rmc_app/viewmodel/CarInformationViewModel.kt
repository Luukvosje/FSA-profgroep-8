package com.profgroep8.rmc_app.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.network.interfaces.services.ServiceFactory
import com.example.network.models.domain.Car
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CarInformationUiState(
    val car: Car? = null
)

class CarInformationViewModel(
    private val sf : ServiceFactory,
    val carId: Int
): BaseViewModel() {

    private val _uiState = MutableStateFlow(CarInformationUiState())
    val uiState: StateFlow<CarInformationUiState> = _uiState.asStateFlow()

    init {
        getCar(carId)
    }

    private fun getCar(carId: Int) {
        viewModelScope.launch {
            withLoading {
                val cars = sf.carService.getSingleCar(carId);
                cars.onSuccess { items ->
                    _uiState.update { it.copy(car = items) }
                }
            }
        }
    }
}