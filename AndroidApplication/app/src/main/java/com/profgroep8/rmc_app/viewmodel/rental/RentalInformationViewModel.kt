package com.profgroep8.rmc_app.viewmodel.rental

import androidx.lifecycle.viewModelScope
import com.example.network.interfaces.services.ServiceFactory
import com.example.network.models.domain.Car
import com.example.network.models.domain.RentalWithLocations
import com.example.network.services.ApiResult
import com.profgroep8.rmc_app.viewmodel.BaseViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RentalInformationUiState(
    val rental: RentalWithLocations? = null,
    val car: Car? = null,
    val errorMessage: String? = null
)

class RentalInformationViewModel(
    private val serviceFactory: ServiceFactory,
    val rentalId: Int
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(RentalInformationUiState())
    val uiState: StateFlow<RentalInformationUiState> = _uiState.asStateFlow()

    init {
        loadRentalInformation()
    }

    private fun loadRentalInformation() {
        viewModelScope.launch {
            withLoading {
                val rentalDeferred = async { serviceFactory.rentalService.getSingleRental(rentalId) }
                val rentalResult = rentalDeferred.await()

                when (rentalResult) {
                    is ApiResult.Success -> {
                        val rental = rentalResult.data
                        _uiState.update { it.copy(rental = rental) }

                        val carDeferred = async { serviceFactory.carService.getSingleCar(rental.carID) }
                        val imageDeferred = async { serviceFactory.carService.getImage(rental.carID) }

                        val carResult = carDeferred.await()
                        val imageResult = imageDeferred.await()

                        when (carResult) {
                            is ApiResult.Success -> {
                                val car = carResult.data
                                val carWithImage = when (imageResult) {
                                    is ApiResult.Success -> car.copy(imageBytes = imageResult.data)
                                    is ApiResult.Error -> car
                                }
                                _uiState.update { it.copy(car = carWithImage, errorMessage = null) }
                            }
                            is ApiResult.Error -> {
                                _uiState.update {
                                    it.copy(
                                        errorMessage = carResult.exception.message ?: "Failed to load car information"
                                    )
                                }
                            }
                        }
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(
                                errorMessage = rentalResult.exception.message ?: "Failed to load rental information"
                            )
                        }
                    }
                }
            }
        }
    }

    fun retry() {
        _uiState.update { it.copy(errorMessage = null) }
        loadRentalInformation()
    }

    fun endRental(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            withLoading {
                when (val result = serviceFactory.rentalService.endRental(rentalId)) {
                    is ApiResult.Success -> {
                        _uiState.update { it.copy(rental = result.data) }
                        onSuccess()
                    }
                    is ApiResult.Error -> {
                        onError(result.exception.message ?: "Failed to end rental")
                    }
                }
            }
        }
    }
}

