package com.profgroep8.rmc_app.ui.screens.bonuspoints

import RmcFilledButton
import RmcScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.profgroep8.rmc_app.ui.components.RmcSpacer
import com.profgroep8.rmc_app.ui.components.RmcTextField
import com.profgroep8.rmc_app.viewmodel.BonusPointsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BonusPointsScreen(
    navigateToScreen: (String) -> Unit,
    viewModel: BonusPointsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isUnauthorized) {
        if (uiState.isUnauthorized) {
            navigateToScreen(RmcScreen.Login.name)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bonus Points (Database)",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                RmcSpacer(8)
                Text(
                    text = uiState.bonusPoints.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                if (uiState.simulationStatus.isNotBlank()) {
                    RmcSpacer(8)
                    Text(text = uiState.simulationStatus, textAlign = TextAlign.Center)
                }

                uiState.errorMessage?.let { err ->
                    RmcSpacer(8)
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }

                RmcSpacer(20)

                RmcTextField(
                    label = "Start address",
                    value = uiState.startAddress,
                    onValueChange = { viewModel.onStartAddressChanged(it) }
                )
                RmcSpacer(8)

                RmcTextField(
                    label = "Destination address",
                    value = uiState.endAddress,
                    onValueChange = { viewModel.onEndAddressChanged(it) }
                )

                RmcSpacer(16)

                RmcFilledButton(
                    value = if (!uiState.isSimulationRunning) "Start simulation" else "Stop simulation",
                    onClick = {
                        if (!uiState.isSimulationRunning) viewModel.startSimulation()
                        else viewModel.stopSimulation()
                    },
                    isEnabled = !uiState.isLoading
                )

                RmcSpacer(24)

                Text(text = uiState.speedText)
                Text(text = uiState.rpmText)
                Text(text = uiState.gearText)
                Text(text = uiState.scoreText)
                Text(text = uiState.simBonusText)
                Text(text = uiState.modeText)

                RmcSpacer(24)

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RmcFilledButton(
                        value = "Back",
                        onClick = { navigateToScreen(RmcScreen.Home.name) }
                    )
                    RmcFilledButton(
                        value = "Logout",
                        onClick = {
                            viewModel.logout()
                            navigateToScreen(RmcScreen.Welcome.name)
                        }
                    )
                }

                RmcSpacer(24)
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}