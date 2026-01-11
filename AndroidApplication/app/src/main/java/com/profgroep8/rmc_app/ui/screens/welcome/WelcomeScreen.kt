package com.profgroep8.rmc_app.presentation.screens.welcome

import LogoComponent
import RmcFilledButton
import RmcFilledTonalButton
import RmcScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.ui.components.RmcSpacer

@Composable
fun WelcomeScreen(
    navigateToScreen: (String) -> Unit
) {
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .align(Alignment.TopStart)
                    .verticalScroll(rememberScrollState())
            ) {
                LogoComponent()

                RmcSpacer()

                Text(
                    text = stringResource(R.string.welcome_title),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                RmcSpacer(16)

                Column(
                    modifier = Modifier.weight(2f),
                    verticalArrangement = Arrangement.Center
                ) {
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
