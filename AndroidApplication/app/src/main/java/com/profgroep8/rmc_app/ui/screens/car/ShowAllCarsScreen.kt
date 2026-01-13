package com.profgroep8.rmc_app.ui.screens.car

import RmcScreen
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.network.models.domain.Car
import com.example.network.models.domain.CarAvailabilityUi
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.ui.components.RmcAppBar
import com.profgroep8.rmc_app.ui.components.RmcSpacer
import com.profgroep8.rmc_app.viewmodel.car.ShowAllCarsUiState
import com.profgroep8.rmc_app.viewmodel.car.ShowAllCarsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Preview(showBackground = true)
@Composable
fun AllCarsScreenPreview() {
    CarScreenContent(
        state = ShowAllCarsUiState(
            cars = listOf(

            )
        ),
        navigateToScreen = {},
        isLoading = false,
        refreshCars = {},
        onDeleteClick = {}
    )
}



@Composable
fun AllCarsScreen(
    viewModel: ShowAllCarsViewModel = koinViewModel(),
    navigateToScreen: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    CarScreenContent(
        state = uiState,
        navigateToScreen = navigateToScreen,
        isLoading = isLoading,
        refreshCars = { viewModel.refreshCars() },
        onDeleteClick = { viewModel.deleteCar(it) }
    )
}

@Composable
fun CarScreenContent(
    state: ShowAllCarsUiState,
    navigateToScreen: (String) -> Unit,
    isLoading: Boolean,
    refreshCars: () -> Unit,
    onDeleteClick: (Car) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            RmcAppBar(
                title = stringResource(R.string.home_manage_cars),
                subtitle = stringResource(R.string.manage_cars_sub),
                navigationIcon = Icons.AutoMirrored.Filled.ArrowLeft,
                onNavigateUp = { navigateToScreen(RmcScreen.Home.name) }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(id = R.dimen.padding_small))
            ) {
                RmcSpacer()

                PullToRefreshBox(
                    isRefreshing = isLoading,
                    onRefresh = refreshCars,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (state.cars.count() > 0) {
                            items(state.cars) { car ->
                                CarItem(
                                    car = car,
                                    onDeleteClick = { onDeleteClick(car) },
                                    onClick = { navigateToScreen("${RmcScreen.CarInformation.name}/${car.carID}") },
                                )
                            }
                        } else {
                            item {
                                Text(
                                    text = stringResource(R.string.no_cars_found),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
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

@Composable
fun CarItem(
    car: Car,
    carAvailability: CarAvailabilityUi? = null,
    onClick: (Car) -> Unit,
    onDeleteClick: ((Car) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClickLabel = "Open car details",
                role = Role.Button
            ) { onClick(car) },
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${car.brand} ${car.model}",
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = "${car.licensePlate} • ${car.year} • ${car.fuelType.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "€${car.price}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            carAvailability?.let {
                AvailabilityBadge(it)
            }
    if(onDeleteClick != null){
            IconButton(
                onClick = { onDeleteClick(car) }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete car",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
    }
}


@Composable
private fun AvailabilityBadge(
    availability: CarAvailabilityUi
) {
    val (text, color) = if (availability.isAvailable) {
        "Beschikbaar" to MaterialTheme.colorScheme.primary
    } else {
        "Niet beschikbaar" to MaterialTheme.colorScheme.error
    }

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = color.copy(alpha = 0.12f)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        if (!availability.isAvailable && availability.availableFrom != null) {
            Text(
                text = "vanaf ${availability.availableFrom}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


