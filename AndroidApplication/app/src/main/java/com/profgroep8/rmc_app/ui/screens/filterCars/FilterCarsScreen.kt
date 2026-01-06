package com.profgroep8.rmc_app.ui.screens.filterCars

import RmcFilledButton
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profgroep8.rmc_app.ui.components.RmcAppBar
import com.profgroep8.rmc_app.ui.components.RmcSpacer
import com.profgroep8.rmc_app.ui.components.RmcTextField
import com.profgroep8.rmc_app.viewmodel.FilterCarsViewModel
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.network.models.domain.FilterCar
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.ui.screens.showCars.CarItem

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
    onSearch: () -> Unit,
    onReset: () -> Unit,
    navigateToScreen: (String) -> Unit
) {
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(id = R.dimen.padding_small))
            ) {

                /** FILTER INPUTS **/
                RmcTextField(
                    value = state.filter.licensePlate.orEmpty(),
                    label = stringResource(R.string.license_plate),
                    onValueChange = {
                        onFilterChange(state.filter.copy(licensePlate = it))
                    }
                )

                RmcSpacer()

                RmcTextField(
                    value = state.filter.brand.orEmpty(),
                    label = stringResource(R.string.brand),
                    onValueChange = {
                        onFilterChange(state.filter.copy(brand = it))
                    }
                )

                RmcSpacer()

                RmcTextField(
                    value = state.filter.model.orEmpty(),
                    label = stringResource(R.string.model),
                    onValueChange = {
                        onFilterChange(state.filter.copy(model = it))
                    }
                )

                RmcSpacer()

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

                RmcSpacer()

                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                        onClick = onSearch
                    )
                }

                RmcSpacer()

                /** RESULTS **/
                PullToRefreshBox(
                    isRefreshing = isLoading,
                    onRefresh = onSearch,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (state.cars.isNotEmpty()) {
                            items(state.cars) { car ->
                                CarItem(
                                    car = car,
                                    onClick = {
                                        navigateToScreen(
                                            "${RmcScreen.CarInformation.name}/${car.carID}"
                                        )
                                    },
                                    onDeleteClick = {}
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
}