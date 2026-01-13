package com.profgroep8.rmc_app.ui.screens.filterCars

import com.example.network.models.domain.CarAvailabilityUi
import com.example.network.models.domain.FilterCar
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