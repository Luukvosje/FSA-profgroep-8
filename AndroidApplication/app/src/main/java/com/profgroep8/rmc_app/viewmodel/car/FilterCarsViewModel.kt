package com.profgroep8.rmc_app.viewmodel.car

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.network.interfaces.services.ServiceFactory
import com.example.network.models.domain.CarAvailabilityUi
import com.example.network.models.domain.FilterCar
import com.profgroep8.rmc_app.viewmodel.BaseViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

data class FilterCarsUiState(
    val filter: FilterCar = FilterCar(),
    val date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val cars: List<CarAvailabilityUi> = emptyList(),
    val hasSearched: Boolean = false
)
class FilterCarsViewModel(
    private val sf: ServiceFactory
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(FilterCarsUiState())
    val uiState: StateFlow<FilterCarsUiState> = _uiState.asStateFlow()

    fun updateFilter(filter: FilterCar) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun updateDate(date: LocalDate) {
        _uiState.update {
            it.copy(date = date)
        }
    }

    fun resetFilters() {
        _uiState.update { it.copy(filter = FilterCar(), cars = emptyList(), hasSearched = false) }
    }

    fun searchCars() {
        val state = _uiState.value
        val selectedDate = state.date

        viewModelScope.launch {
            withLoading {
                val availabilityDeferred = async {
                    sf.carService.getAllAvailableCars(selectedDate)
                }

                val carsDeferred = async {
                    sf.carService.filterCars(state.filter)
                }

                val availabilityResponse = availabilityDeferred.await()
                val carsResponse = carsDeferred.await()

                carsResponse.onSuccess { cars ->
                    availabilityResponse.onSuccess { availabilityList ->
                        val availabilityMap = availabilityList.associateBy(
                            keySelector = { it.car.carID },
                            valueTransform = { it.availableFrom }
                        )

                        val untilMap =availabilityList.associateBy(
                            keySelector = { it.car.carID },
                            valueTransform = { it.availableUntill }
                        )

                        val uiCars = cars.map { car ->
                            val availableFrom = availabilityMap[car.carID]
                            val availableUntil = untilMap[car.carID]
                            val isAvailable = (availableFrom == null || availableFrom <= selectedDate) && (availableUntil == null || availableUntil >= selectedDate)

                            CarAvailabilityUi(
                                car = car,
                                availableFrom = availableFrom,
                                availableUntil = availableUntil,
                                isAvailable = isAvailable
                            )
                        }

                        _uiState.update {
                            it.copy(
                                cars = uiCars,
                                hasSearched = true
                            )
                        }
                    }
                }

                carsResponse.onError {
                    Log.e("FilterCarsVM", "Filter error: $it")
                }

                availabilityResponse.onError {
                    Log.e("FilterCarsVM", "Availability error: $it")
                }
            }
        }
    }

}
