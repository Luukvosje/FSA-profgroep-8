package com.profgroep8.rmc_app.presentation.screens.welcome

import LogoComponent
import RmcFilledButton
import RmcFilledTonalButton
import RmcScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.ui.components.RmcSpacer
import com.profgroep8.rmc_app.viewmodel.user.WelcomeViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WelcomeScreen(
    navigateToScreen: (String) -> Unit,
    viewModel: WelcomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateToHome) {
        if (uiState.navigateToHome) {
            navigateToScreen(RmcScreen.Home.name)
            viewModel.onNavigationHandled()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(dimensionResource(R.dimen.padding_large))
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LogoComponent()

                    RmcSpacer()

                    Text(
                        text = stringResource(R.string.welcome_title),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    RmcFilledTonalButton(
                        value = stringResource(R.string.register),
                        onClick = { navigateToScreen(RmcScreen.Register.name) }
                    )

                    RmcSpacer(8)

                    RmcFilledButton(
                        value = stringResource(R.string.login),
                        onClick = { navigateToScreen(RmcScreen.Login.name) }
                    )
                }
            }
        }
    }
}