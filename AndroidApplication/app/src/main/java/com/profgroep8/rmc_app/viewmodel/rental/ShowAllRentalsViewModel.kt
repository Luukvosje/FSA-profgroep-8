package com.profgroep8.rmc_app.viewmodel.rental

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.interfaces.services.ServiceFactory
import com.example.network.models.domain.RentalWithCarInfo
import com.example.network.services.ApiResult
import com.profgroep8.rmc_app.ui.screens.rental.ShowAllRentalsUIEvent
import com.profgroep8.rmc_app.ui.screens.rental.ShowAllRentalsUIState
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ShowAllRentalsViewModel(
    private val serviceFactory: ServiceFactory
) : ViewModel() {
    private val _uiState = MutableStateFlow(ShowAllRentalsUIState())
    val uiState: StateFlow<ShowAllRentalsUIState> = _uiState.asStateFlow()

    init {
        loadRentals()
    }

    fun onEvent(event: ShowAllRentalsUIEvent) {
        when (event) {
            is ShowAllRentalsUIEvent.LoadRentals -> loadRentals()
            is ShowAllRentalsUIEvent.Retry -> {
                _uiState.update { it.copy(errorMessage = null) }
                loadRentals()
            }
            is ShowAllRentalsUIEvent.ClearError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun loadRentals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = serviceFactory.rentalService.getAllRentals()) {
                is ApiResult.Success -> {
                    val rentalsWithCarInfo = result.data.map { rental ->
                        async {
                            val carResult = serviceFactory.carService.getSingleCar(rental.carID)
                            val car = when (carResult) {
                                is ApiResult.Success -> carResult.data
                                is ApiResult.Error -> null
                            }
                            RentalWithCarInfo(rental = rental, car = car)
                        }
                    }.awaitAll()
                    
                    _uiState.update {
                        it.copy(
                            rentals = rentalsWithCarInfo,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.message ?: "Failed to load rentals"
                        )
                    }
                }
            }
        }
    }
}