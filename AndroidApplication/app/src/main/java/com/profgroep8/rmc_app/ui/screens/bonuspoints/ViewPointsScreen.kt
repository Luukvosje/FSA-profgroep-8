package com.profgroep8.rmc_app.ui.screens.bonuspoints

import RmcFilledButton
import RmcScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.profgroep8.rmc_app.viewmodel.BonusPointsViewModel

@Composable
fun BonusPointsScreen(
    navigateToScreen: (String) -> Unit,
    viewModel: BonusPointsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val speedText by viewModel.simSpeedText.collectAsState()
    val rpmText by viewModel.simRpmText.collectAsState()
    val gearText by viewModel.simGearText.collectAsState()
    val scoreText by viewModel.simScoreText.collectAsState()
    val simBonusText by viewModel.simBonusText.collectAsState()
    val modeText by viewModel.simModeText.collectAsState()

    // redirect if token invalid
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
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Bonus Points (Database)",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.bonusPoints.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))

                RmcFilledButton(
                    value = if (!uiState.isSimulationRunning) "Start Real Car Simulation" else "Stop Simulation",
                    onClick = {
                        if (!uiState.isSimulationRunning) viewModel.startSimulation()
                        else viewModel.stopSimulation()
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = speedText)
                Text(text = rpmText)
                Text(text = gearText)
                Text(text = scoreText)
                Text(text = simBonusText)
                Text(text = modeText)

                Spacer(modifier = Modifier.height(32.dp))

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

                uiState.errorMessage?.let { err ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
