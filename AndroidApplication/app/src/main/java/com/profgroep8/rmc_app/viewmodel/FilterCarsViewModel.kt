package com.profgroep8.rmc_app.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.network.interfaces.services.ServiceFactory
import com.example.network.models.domain.Car
import com.example.network.models.domain.FilterCar
import com.profgroep8.rmc_app.ui.screens.filterCars.FilterCarsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FilterCarsViewModel(
    private val sf: ServiceFactory
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(FilterCarsUiState())
    val uiState: StateFlow<FilterCarsUiState> = _uiState.asStateFlow()

    fun updateFilter(filter: FilterCar) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun resetFilters() {
        _uiState.update { it.copy(filter = FilterCar(), cars = emptyList(), hasSearched = false) }
    }

    fun searchCars() {
        val filter = _uiState.value.filter
        viewModelScope.launch {
            withLoading {
                val response = sf.carService.filterCars(_uiState.value.filter)
                response.onSuccess { cars ->
                    _uiState.update { it.copy(cars = cars, hasSearched = true) }
                }
                response.onError { error ->
                    Log.e("FilterCarsVM", "API error: $error")
                }
            }
        }
    }
}
