package com.profgroep8.rmc_app.ui.screens.rental

import RmcScreen
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.network.models.domain.RentalWithCarInfo
import com.example.network.services.UserProvider
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.ui.components.RmcAppBar
import com.profgroep8.rmc_app.ui.components.RmcSpacer
import com.profgroep8.rmc_app.viewmodel.rental.ShowAllRentalsViewModel
import org.koin.compose.viewmodel.koinViewModel

data class ShowAllRentalsUIState(
    val rentals: List<RentalWithCarInfo> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface ShowAllRentalsUIEvent {
    object LoadRentals : ShowAllRentalsUIEvent
    object Retry : ShowAllRentalsUIEvent
    object ClearError : ShowAllRentalsUIEvent
}

@Preview(showBackground = true)
@Composable
fun AllRentalsScreenPreview() {
    AllRentalsScreen(
        navigateToScreen = {},
    )
}

@Composable
fun AllRentalsScreen(
    viewModel: ShowAllRentalsViewModel = koinViewModel(),
    navigateToScreen: (String) -> Unit,
    navigateBack: () -> Unit = { navigateToScreen(RmcScreen.Home.name) }
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler {
        navigateBack()
    }

    Scaffold (
        topBar = {
            RmcAppBar (
                title = stringResource(R.string.all_rentals),
                subtitle = stringResource(R.string.all_rentals_sub),
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                onNavigateUp = { navigateToScreen(RmcScreen.Home.name) }
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
                uiState.isLoading -> {
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
                            onClick = { viewModel.onEvent(ShowAllRentalsUIEvent.Retry) }
                        ) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
                uiState.rentals.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_rentals_found),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                else -> {
                    RentalsDisplay(
                        rentals = uiState.rentals,
                        navigateToScreen = navigateToScreen
                    )
                }
            }
        }
    }
}

@Composable
private fun RentalsDisplay(
    rentals: List<RentalWithCarInfo>,
    navigateToScreen: (String) -> Unit
) {
    val currentUserId = UserProvider.user?.userID
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = dimensionResource(R.dimen.padding_large),
            vertical = dimensionResource(R.dimen.padding_medium)
        ),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
    ) {
        items(
            items = rentals,
            key = { it.rental.rentalID }
        ) { rentalWithCar ->
            val isOwner = currentUserId != null && rentalWithCar.car?.userID == currentUserId
            val isRenter = currentUserId != null && rentalWithCar.rental.userID == currentUserId

            SingleRentalListItem(
                rentalWithCar = rentalWithCar,
                onClick = { navigateToScreen("${RmcScreen.RentalInformation.name}/${rentalWithCar.rental.rentalID}") },
                isOwner = isOwner,
                isRenter = isRenter
            )
        }
    }
}

@Composable
fun SingleRentalListItem(
    rentalWithCar: RentalWithCarInfo,
    onClick: () -> Unit,
    isOwner: Boolean = false,
    isRenter: Boolean = false
) {
    val rental = rentalWithCar.rental
    val car = rentalWithCar.car

    val roleLabel = when {
        isOwner -> stringResource(R.string.renting_out)
        isRenter -> stringResource(R.string.renting)
        else -> null
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(
                onClick = onClick,
                role = Role.Button
            ),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_large))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (car != null) {
                        Text(
                            text = "${car.licensePlate} ${car.brand} ${car.model}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        RmcSpacer(4)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = car.year.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                text = car.fuelType.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.no_car_found),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (roleLabel != null) {
                        RmcSpacer(4)
                        Text(
                            text = roleLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Text(
                    text = when (rental.state) {
                        0 -> stringResource(R.string.completed)
                        1 -> stringResource(R.string.rented)
                        else -> stringResource(R.string.unknown)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}