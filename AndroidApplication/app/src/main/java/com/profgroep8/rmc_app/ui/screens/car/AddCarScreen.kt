package com.profgroep8.rmc_app.ui.screens.car

import RmcFilledButton
import RmcScreen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.ui.components.RmcAppBar
import com.profgroep8.rmc_app.viewmodel.car.AddCarViewModel
import org.koin.compose.viewmodel.koinViewModel

@Preview
@Composable
fun AddCarScreenPreview() {
    AddCarScreen(
        viewModel = viewModel(),
        navigateToScreen = { string -> println(string) }
    )
}

@Composable
fun AddCarScreen(
    viewModel: AddCarViewModel = koinViewModel(),
    navigateToScreen: (String) -> Unit
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val loading by viewModel.isLoading.collectAsStateWithLifecycle()


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                RmcAppBar(
                    title = stringResource(R.string.add_car),
                    subtitle = stringResource(R.string.add_car_sub),
                    navigationIcon = Icons.AutoMirrored.Filled.ArrowLeft,
                    onNavigateUp = { navigateToScreen(RmcScreen.Home.name) }
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(id = R.dimen.padding_small))
                ) {
                    KeyboardOptions(
                        imeAction = ImeAction.Done
                    )
                    KeyboardActions(
                        onDone = { viewModel.addCar(navigateToScreen) }
                    )
                    Column() {
                        FormTextField(
                            label = stringResource(R.string.license_plate),
                            value = state.licensePlate,
                            onValueChange = viewModel::onLicensePlateChange,
                            error = state.licensePlateError?.asString()
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding(),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        RmcFilledButton(
                            value = stringResource(R.string.button_next),
                            isEnabled = state.isValid,
                            onClick = { viewModel.addCar(navigateToScreen) }
                        )
                    }
                }
            }
    }
}

@Composable
fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            isError = error != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
