package com.profgroep8.rmc_app.ui.screens.rental

import RmcFilledButton
import RmcScreen
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.network.models.domain.Car
import com.example.network.models.domain.RentalWithLocations
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.ui.components.CarInfoItem
import com.profgroep8.rmc_app.ui.components.RmcAppBar
import com.profgroep8.rmc_app.ui.components.RmcSpacer
import com.example.network.services.UserProvider
import com.profgroep8.rmc_app.utils.formatDateTime
import com.profgroep8.rmc_app.utils.formatLocation
import com.profgroep8.rmc_app.viewmodel.rental.RentalInformationViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Preview(showBackground = true)
@Composable
fun RentalInformationScreenPreview() {
    RentalInformationScreen(
        rentalId = 1,
        navigateToScreen = {}
    )
}

@Composable
fun RentalInformationScreen(
    rentalId: Int?,
    navigateToScreen: (String) -> Unit,
    viewModel: RentalInformationViewModel = koinViewModel(parameters = { parametersOf(rentalId ?: 0) }),
    navigateBack: () -> Unit = { navigateToScreen(RmcScreen.Rentals.name) }
) {
    if (rentalId == null) {
        navigateToScreen(RmcScreen.Rentals.name)
        return
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val loading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    var showEndRentalDialog by remember { mutableStateOf(false) }
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }

    if (showEndRentalDialog) {
        AlertDialog(
            onDismissRequest = { showEndRentalDialog = false },
            title = { Text(stringResource(R.string.end_rental_confirmation_title)) },
            text = { Text(stringResource(R.string.end_rental_confirmation_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEndRentalDialog = false
                        viewModel.endRental(
                            onSuccess = { navigateToScreen(RmcScreen.Rentals.name) },
                            onError = { error -> errorDialogMessage = error }
                        )
                    }
                ) {
                    Text(stringResource(R.string.button_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndRentalDialog = false }) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        )
    }

    errorDialogMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { errorDialogMessage = null },
            title = { Text(stringResource(R.string.error)) },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { errorDialogMessage = null }) {
                    Text(stringResource(R.string.button_ok))
                }
            }
        )
    }

    BackHandler {
        navigateBack()
    }

    Scaffold(
        topBar = {
            RmcAppBar(
                title = stringResource(R.string.rental_information),
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                onNavigateUp = navigateBack
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            when {
                loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(dimensionResource(R.dimen.padding_large)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.errorMessage ?: stringResource(R.string.unknown_error),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        RmcSpacer(16)
                        TextButton(
                            onClick = { viewModel.retry() }
                        ) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
                else -> {
                    RentalInformationContent(
                        rental = uiState.rental,
                        car = uiState.car,
                        navigateToScreen = navigateToScreen,
                        onEndRental = { showEndRentalDialog = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun RentalInformationContent(
    rental: RentalWithLocations?,
    car: Car?,
    navigateToScreen: (String) -> Unit,
    onEndRental: () -> Unit
) {
    val currentUserId = UserProvider.user?.userID
    val isOwner = currentUserId != null && car?.userID == currentUserId
    val isRenter = currentUserId != null && rental?.userID == currentUserId
    val isActiveRental = rental?.state == 1

    val roleLabel = when {
        isOwner -> stringResource(R.string.renting_out)
        isRenter -> stringResource(R.string.renting)
        else -> null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(dimensionResource(R.dimen.padding_large))
    ) {
        if (car != null) {
            CarImageSection(car = car)
            RmcSpacer(24)
        }

        Text(
            text = stringResource(R.string.car_information),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        RmcSpacer(16)

        CarInfoItem(
            label = stringResource(R.string.license_plate),
            value = car?.licensePlate,
            loading = car == null
        )

        CarInfoItem(
            label = stringResource(R.string.brand),
            value = car?.brand,
            loading = car == null
        )

        CarInfoItem(
            label = stringResource(R.string.model),
            value = car?.model,
            loading = car == null
        )

        CarInfoItem(
            label = stringResource(R.string.year),
            value = car?.year?.toString(),
            loading = car == null
        )

        CarInfoItem(
            label = stringResource(R.string.fuelType),
            value = car?.fuelType?.displayName,
            loading = car == null
        )

        if (car != null) {
            CarInfoItem(
                label = stringResource(R.string.price),
                value = "€${car.price}",
                loading = false
            )
        }

        RmcSpacer(32)

        Text(
            text = stringResource(R.string.rental_information),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        RmcSpacer(16)

        if (roleLabel != null) {
            CarInfoItem(
                label = "${stringResource(R.string.renting_out)}/${stringResource(R.string.renting)}:",
                value = roleLabel,
                loading = false
            )
        }

        if (rental != null) {
            CarInfoItem(
                label = stringResource(R.string.start_date),
                value = formatDateTime(rental.startRentalLocation.date),
                loading = false
            )

            CarInfoItem(
                label = stringResource(R.string.end_date),
                value = formatDateTime(rental.endRentalLocation.date),
                loading = false
            )

            CarInfoItem(
                label = stringResource(R.string.start_location),
                value = formatLocation(rental.startRentalLocation.latitude, rental.startRentalLocation.longitude),
                loading = false
            )

            CarInfoItem(
                label = stringResource(R.string.end_location),
                value = formatLocation(rental.endRentalLocation.latitude, rental.endRentalLocation.longitude),
                loading = false
            )

            CarInfoItem(
                label = stringResource(R.string.status),
                value = when (rental.state) {
                    0 -> stringResource(R.string.completed)
                    1 -> stringResource(R.string.rented)
                    else -> stringResource(R.string.unknown)
                },
                loading = false
            )
        }

        RmcSpacer(32)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isRenter && isActiveRental) {
                RmcFilledButton(
                    value = stringResource(R.string.end_rental),
                    onClick = onEndRental
                )
                RmcSpacer(8)
            }
            
            RmcFilledButton(
                value = stringResource(R.string.button_back),
                onClick = { navigateToScreen(RmcScreen.Rentals.name) }
            )
        }
    }
}

@Composable
private fun CarImageSection(car: Car) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (car.imageBytes != null) {
            AsyncImage(
                model = car.imageBytes,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = stringResource(R.string.no_photo_found),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}