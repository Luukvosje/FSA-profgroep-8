package com.profgroep8.rmc_app.presentation.screens.register

import RmcFilledButton
import RmcScreen
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.presentation.components.ClickableLoginTextComponent
import com.profgroep8.rmc_app.presentation.components.DividerTextComponent
import com.profgroep8.rmc_app.presentation.components.RmcAppBar
import com.profgroep8.rmc_app.presentation.components.RmcSpacer
import com.profgroep8.rmc_app.presentation.components.RmcTextField
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = koinViewModel(),
    navigateToScreen: (String) -> Unit,
    navigateBack: () -> Unit = { navigateToScreen(RmcScreen.Welcome.name) }
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler {
        navigateBack()
    }

    Scaffold (
        topBar = {
            RmcAppBar (
                title = stringResource(R.string.register),
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                onNavigateUp = navigateBack
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = dimensionResource(R.dimen.padding_large))
                    .verticalScroll(rememberScrollState())
            ) {
                RmcTextField(
                    label = stringResource(id = R.string.email),
                    leadingIcon = Icons.Filled.Email,
                    value = uiState.email,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    onValueChange = { viewModel.onEvent(RegisterUIEvent.EmailChanged(it)) }
                )

                RmcSpacer(8)

                RmcTextField(
                    label = stringResource(id = R.string.password),
                    leadingIcon = Icons.Filled.Lock,
                    value = uiState.password,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    isPassword = true,
                    onValueChange = { viewModel.onEvent(RegisterUIEvent.PasswordChanged(it)) }
                )

                RmcSpacer(8)

                RmcTextField(
                    label = "Naam",
                    value = uiState.fullName,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    onValueChange = { viewModel.onEvent(RegisterUIEvent.FullNameChanged(it)) }
                )

                RmcSpacer(8)

                RmcTextField(
                    label = "Telefoonnummer",
                    value = uiState.phone,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Phone
                    ),
                    onValueChange = { viewModel.onEvent(RegisterUIEvent.PhoneChanged(it)) }
                )

                RmcSpacer(8)

                RmcTextField(
                    label = "Adres",
                    value = uiState.address,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    onValueChange = { viewModel.onEvent(RegisterUIEvent.AddressChanged(it)) }
                )

                RmcSpacer(8)

                RmcTextField(
                    label = "Postcode",
                    value = uiState.zipcode,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    onValueChange = { viewModel.onEvent(RegisterUIEvent.ZipcodeChanged(it)) }
                )

                RmcSpacer(8)

                RmcTextField(
                    label = "Stad",
                    value = uiState.city,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    onValueChange = { viewModel.onEvent(RegisterUIEvent.CityChanged(it)) }
                )

                RmcSpacer(8)

                RmcTextField(
                    label = "Landcode (bv. NL)",
                    value = uiState.countryISO,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    onValueChange = { viewModel.onEvent(RegisterUIEvent.CountryISOChanged(it)) }
                )

                RmcSpacer(16)

                RmcFilledButton(
                    value = stringResource(id = R.string.register),
                    isEnabled = uiState.email.isNotEmpty() && uiState.password.isNotEmpty(),
                    onClick = { viewModel.onEvent(RegisterUIEvent.RegisterButtonClicked) }
                )

                DividerTextComponent()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ClickableLoginTextComponent(
                        tryingToLogin = true,
                        onTextSelected = { navigateToScreen(RmcScreen.Login.name) }
                    )
                }

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}