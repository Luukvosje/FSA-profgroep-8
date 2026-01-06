package com.profgroep8.rmc_app.ui.screens.filterCars

import com.example.network.models.domain.Car
import com.example.network.models.domain.FilterCar

data class FilterCarsUiState(
    val filter: FilterCar = FilterCar(),
    val cars: List<Car> = emptyList(),
    val hasSearched: Boolean = false
)