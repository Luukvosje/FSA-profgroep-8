package com.profgroep8.rmc_app.presentation.screens.home

import RmcFilledButton
import RmcFilledTonalButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.presentation.components.RmcSpacer

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        navigateToScreen = {},
        userName = "Loek (TEST)"
    )
}

@Composable
fun HomeScreen(
    navigateToScreen: (String) -> Unit,
    userName: String
) {
    val message = stringResource(id = R.string.home_message, userName)

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
            RmcFilledButton(
                value = stringResource(R.string.logout),
                onClick = {  },
                modifier = Modifier.align(Alignment.TopEnd).width(150.dp).height(40.dp)
            )
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
                        value = stringResource(id = R.string.home_add_car),
                        onClick = { navigateToScreen(RmcScreen.Home.name) }
                    )

                    RmcFilledButton(
                        value = stringResource(id = R.string.home_manage_cars),
                        onClick = { navigateToScreen(RmcScreen.Home.name) }
                    )

                    RmcFilledButton(
                        value = stringResource(id = R.string.home_search_car),
                        onClick = { navigateToScreen(RmcScreen.Home.name) }
                    )

                    RmcFilledButton(
                        value = stringResource(id = R.string.home_reservations),
                        onClick = { navigateToScreen(RmcScreen.Home.name) }
                    )

                    RmcFilledButton(
                        value = stringResource(id = R.string.home_view_route),
                        onClick = { navigateToScreen(RmcScreen.Home.name) }
                    )

                    RmcFilledButton(
                        value = stringResource(id = R.string.home_view_points),
                        onClick = { navigateToScreen(RmcScreen.Home.name) }
                    )
                }
            }
        }
    }
}
