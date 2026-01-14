package com.profgroep8.rmc_app.ui.screens.car

import RmcFilledButton
import RmcScreen
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.network.models.domain.FilterCar
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.ui.components.RmcAppBar
import com.profgroep8.rmc_app.ui.components.RmcSpacer
import com.profgroep8.rmc_app.ui.components.RmcTextField
import com.profgroep8.rmc_app.viewmodel.car.FilterCarsUiState
import com.profgroep8.rmc_app.viewmodel.car.FilterCarsViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Instant

@Composable
fun FilterCarsScreen(
    viewModel: FilterCarsViewModel = koinViewModel(),
    navigateToScreen: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    FilterCarsContent(
        state = state,
        isLoading = isLoading,
        onFilterChange = viewModel::updateFilter,
        onDateChange = viewModel::updateDate,
        onSearch = viewModel::searchCars,
        onReset = viewModel::resetFilters,
        navigateToScreen = navigateToScreen
    )
}

@Composable
fun FilterCarsContent(
    state: FilterCarsUiState,
    isLoading: Boolean,
    onFilterChange: (FilterCar) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onSearch: () -> Unit,
    onReset: () -> Unit,
    navigateToScreen: (String) -> Unit
) {
    var isFilterExpanded by rememberSaveable { mutableStateOf(false) }

    val search: () -> Unit = {
        isFilterExpanded = false
        onSearch()
    }

    LaunchedEffect(Unit) {
        search()
    }
    LaunchedEffect(state.date) {
        if (state.date != null) {
            onSearch()
        }
    }


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RmcAppBar(
                title = stringResource(R.string.filter_cars),
                subtitle = stringResource(R.string.filter_cars_sub),
                navigationIcon = Icons.AutoMirrored.Filled.ArrowLeft,
                onNavigateUp = { navigateToScreen(RmcScreen.Home.name) }
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                RmcFilledButton(
                    value = stringResource(R.string.filters),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(id = R.dimen.padding_small)),
                    onClick = { isFilterExpanded = !isFilterExpanded },
                    icon = if (isFilterExpanded)
                        Icons.Filled.KeyboardArrowUp
                    else
                        Icons.Filled.KeyboardArrowDown,
                )
            }

            AnimatedVisibility(
                visible = isFilterExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(id = R.dimen.padding_small))
                ) {

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            /** FILTER INPUTS **/
                            RmcTextField(
                                value = state.filter.licensePlate.orEmpty(),
                                label = stringResource(R.string.license_plate),
                                onValueChange = {
                                    onFilterChange(state.filter.copy(licensePlate = it))
                                }
                            )
                        }
                        item {
                            RmcTextField(
                                value = state.filter.brand.orEmpty(),
                                label = stringResource(R.string.brand),
                                onValueChange = {
                                    onFilterChange(state.filter.copy(brand = it))
                                }
                            )
                        }
                        item {
                            RmcTextField(
                                value = state.filter.model.orEmpty(),
                                label = stringResource(R.string.model),
                                onValueChange = {
                                    onFilterChange(state.filter.copy(model = it))
                                }
                            )
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                RmcTextField(
                                    value = state.filter.minPrice?.toString().orEmpty(),
                                    label = stringResource(R.string.min_price),
                                    modifier = Modifier.weight(1f),
                                    onValueChange = { input ->
                                        val value = input.toDoubleOrNull()
                                        onFilterChange(state.filter.copy(minPrice = value))
                                    }
                                )

                                RmcTextField(
                                    value = state.filter.maxPrice?.toString().orEmpty(),
                                    label = stringResource(R.string.max_price),
                                    modifier = Modifier.weight(1f),
                                    onValueChange = { input ->
                                        val value = input.toDoubleOrNull()
                                        onFilterChange(state.filter.copy(maxPrice = value))
                                    }
                                )
                            }
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RmcTextField(
                                    value = state.filter.year?.toString().orEmpty(),
                                    label = stringResource(R.string.car_year),
                                    modifier = Modifier.weight(1f),
                                    onValueChange = { input ->
                                        val value = input.toIntOrNull()
                                        onFilterChange(state.filter.copy(year = value))
                                    }
                                )
                            }
                        }
                        item {
                            DatePickerField(
                                label = stringResource(R.string.rent_from_date),
                                date = state.date,
                                onDateSelected = {
                                    onDateChange(it)
                                }
                            )
                        }
                    }
                    RmcSpacer()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding()
                            .height(40.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RmcFilledButton(
                            value = stringResource(R.string.reset),
                            modifier = Modifier.weight(1f),
                            onClick = onReset
                        )
                        RmcFilledButton(
                            value = stringResource(R.string.search),
                            modifier = Modifier.weight(1f),
                            onClick = search
                        )

                    }
                }
            }
                /** RESULTS **/
                PullToRefreshBox(
                    isRefreshing = isLoading,
                    onRefresh = onSearch,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(id = R.dimen.padding_small))
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (state.cars.isNotEmpty()) {
                            items(state.cars) { carAvailability ->
                                CarItem(
                                    car = carAvailability.car,
                                    carAvailability = carAvailability,
                                    onClick = {
                                        navigateToScreen(
                                            "${RmcScreen.CarInformation.name}/${carAvailability.car.carID}/${carAvailability.isAvailable}"
                                        )
                                    },
                                )
                            }
                        } else if (state.hasSearched) {
                            item {
                                Text(
                                    text = stringResource(R.string.no_cars_found),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
    }
}

@Composable
fun DatePickerField(
    label: String,
    date: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    val formattedDate = date?.toString() ?: ""

    Column {
        RmcTextField(
            value = formattedDate,
            label = label,
            readOnly = true,
            enabled = false,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().clickable { showDialog = true },
                    trailingIcon = Icons.Filled.CalendarToday,
        )

        if (showDialog) {
            val datePickerState = rememberDatePickerState()

            DatePickerDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    RmcFilledButton(
                        value = stringResource(R.string.confirm),
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val localDate: LocalDate =
                                    Instant.fromEpochMilliseconds(millis)
                                        .toLocalDateTime(TimeZone.currentSystemDefault())
                                        .date

                                onDateSelected(localDate)
                            }
                            showDialog = false
                        }
                    )
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
