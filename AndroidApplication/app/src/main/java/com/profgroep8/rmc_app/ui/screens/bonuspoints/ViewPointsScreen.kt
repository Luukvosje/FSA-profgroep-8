package com.profgroep8.rmc_app.ui.screens.bonuspoints

import RmcFilledButton
import RmcScreen
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
    viewModel: BonusPointsViewModel = viewModel() // ✅ SAME AS LOGIN
) {
    val uiState by viewModel.uiState.collectAsState()

    // 🔐 Redirect if not logged in
    LaunchedEffect(uiState.isUnauthorized) {
        if (uiState.isUnauthorized) {
            navigateToScreen(RmcScreen.Login.name)
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }

                uiState.bonusPoints != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "You have ${uiState.bonusPoints} bonus points",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        RmcFilledButton(
                            value = "Back to Home",
                            onClick = {
                                navigateToScreen(RmcScreen.Home.name)
                            }
                        )
                    }
                }

                uiState.errorMessage != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        RmcFilledButton(
                            value = "Back",
                            onClick = {
                                navigateToScreen(RmcScreen.Home.name)
                            }
                        )
                    }
                }
            }
        }
    }
}