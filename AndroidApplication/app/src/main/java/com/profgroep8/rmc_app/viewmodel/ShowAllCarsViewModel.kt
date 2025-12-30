    package com.profgroep8.rmc_app.viewmodel

    import androidx.lifecycle.viewModelScope
import com.example.network.interfaces.services.ServiceFactory
import com.example.network.models.domain.Car
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

    data class ShowAllCarsUiState(
        val cars: List<Car> = listOf()
    )

    class ShowAllCarsViewModel(
        private val sf : ServiceFactory,
    ): BaseViewModel() {

        private val _uiState = MutableStateFlow(ShowAllCarsUiState())
        val uiState: StateFlow<ShowAllCarsUiState> = _uiState.asStateFlow()

        init {
            sf.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJjYXJfcmVudGFsX3VzZXJzIiwiaXNzIjoiY29tLnByb2Zncm9lcDguY2FycmVudGFsIiwidXNlcklkIjoiMSIsImVtYWlsIjoidGVzdEB0ZXN0LmNvbSIsImV4cCI6MTc2NzE3NTQxMX0.tquyqvtbivJqq9tdjlqx9DVn618ULp6V9QpiFiRVYfM")
            getAllCars()
        }

        private fun getAllCars() {
            viewModelScope.launch {
                withLoading {
                    val cars = sf.carService.getAllCars();
                    cars.onSuccess { items ->
                        _uiState.update { it.copy(cars = items) }
                    }
                }
            }
        }

        fun refreshCars () {
            getAllCars()
        }

        fun deleteCar(car: Car){
            viewModelScope.launch {
                withLoading {
                    val res = sf.carService.deleteCar(car.carID)

                    res.onSuccess {
                        if (it) {
                            getAllCars();
                        } else {
                            TODO("Show Toast")
                        }
                    }
                }
            }
        }
    }