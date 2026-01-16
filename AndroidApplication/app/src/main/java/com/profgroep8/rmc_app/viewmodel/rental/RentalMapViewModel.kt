package com.profgroep8.rmc_app.viewmodel.rental

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.interfaces.services.ServiceFactory
import com.example.network.models.domain.RentalWithCarInfo
import com.example.network.services.ApiResult
import com.example.network.services.UserProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RentalMapUiState(
    val rentals: List<RentalWithCarInfo> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class RentalMapViewModel(
    private val serviceFactory: ServiceFactory
) : ViewModel() {

    private val _uiState = MutableStateFlow(RentalMapUiState())
    val uiState: StateFlow<RentalMapUiState> = _uiState.asStateFlow()

    init {
        loadUserRentals()
    }

    fun retry() {
        _uiState.update { it.copy(errorMessage = null) }
        loadUserRentals()
    }

    private fun loadUserRentals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val currentUserId = UserProvider.user?.userID?: return@launch

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

                    val userRentals = rentalsWithCarInfo.filter { rentalWithCar ->
                        val isRenter = rentalWithCar.rental.userID == currentUserId
                        val isOwner = rentalWithCar.car?.userID == currentUserId
                        val isNotCompleted = rentalWithCar.rental.state != 0
                        (isRenter || isOwner) && isNotCompleted
                    }

                    _uiState.update {
                        it.copy(
                            rentals = userRentals,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.message
                        )
                    }
                }
            }
        }
    }
}
