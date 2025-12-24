package com.profgroep8.rmc_app.ui.screens.AddCar

import RmcFilledButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.ui.components.RmcTextField
import org.koin.compose.viewmodel.koinViewModel

@Preview
@Composable
fun AddCarScreenPreview() {
    _root_ide_package_.com.profgroep8.rmc_app.ui.screens.AddCar.AddCarScreen(
        viewModel = viewModel(),
        navigateToScreen = { string -> println(string) }
    )
}

@Composable
fun AddCarScreen(
    viewModel: com.profgroep8.rmc_app.ui.screens.AddCar.AddCarViewModel = koinViewModel(),
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
            ) {
                Text(
                    text = stringResource(R.string.add_car),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "test",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.fillMaxWidth()
                )
                Column() {
                    RmcTextField(stringResource(R.string.license_plate), onValueChange = {})
                }
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
                    RmcFilledButton(value = stringResource(R.string.button_next), onClick = {})
                }
            }

        }
    }
}