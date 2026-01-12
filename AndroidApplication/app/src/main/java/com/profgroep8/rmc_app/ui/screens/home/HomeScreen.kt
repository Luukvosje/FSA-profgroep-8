package com.profgroep8.rmc_app.ui.screens.home

import RmcFilledButton
import RmcScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.ui.components.RmcSpacer
import com.profgroep8.rmc_app.viewmodel.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    navigateToScreen: (String) -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            navigateToScreen(RmcScreen.Welcome.name)
            viewModel.onLogoutHandled()
        }
    }

    val nameToShow = uiState.userName.ifBlank { stringResource(R.string.user) }
    val message = stringResource(id = R.string.home_message, nameToShow)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(dimensionResource(R.dimen.padding_large))
        ) {

            RmcFilledButton(
                value = stringResource(R.string.logout),
                onClick = { viewModel.logout() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .width(150.dp)
                    .height(40.dp)
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    RmcSpacer(16)

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RmcFilledButton(
                            value = stringResource(R.string.home_add_car),
                            onClick = { navigateToScreen(RmcScreen.AddCar.name) }
                        )

                        RmcFilledButton(
                            value = stringResource(R.string.home_manage_cars),
                            onClick = { navigateToScreen(RmcScreen.AllCars.name) }
                        )

                        RmcFilledButton(
                            value = stringResource(R.string.home_search_car),
                            onClick = { navigateToScreen(RmcScreen.FilterCars.name) }
                        )

                        RmcFilledButton(
                            value = stringResource(R.string.home_rentals),
                            onClick = { navigateToScreen(RmcScreen.Rentals.name) }
                        )

                        RmcFilledButton(
                            value = stringResource(R.string.home_view_points),
                            onClick = { navigateToScreen(RmcScreen.ViewPoints.name) }
                        )
                    }
                }
            }
        }
    }
}