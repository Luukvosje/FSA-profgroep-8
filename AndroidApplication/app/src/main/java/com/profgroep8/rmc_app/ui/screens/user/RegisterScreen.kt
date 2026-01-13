package com.profgroep8.rmc_app.ui.screens.user

import RmcFilledButton
import RmcScreen
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.ui.components.*
import com.profgroep8.rmc_app.ui.events.RegisterUIEvent
import com.profgroep8.rmc_app.viewmodel.user.RegisterViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = koinViewModel(),
    navigateToScreen: (String) -> Unit,
    navigateBack: () -> Unit = { navigateToScreen(RmcScreen.Welcome.name) }
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler { navigateBack() }

    if (uiState.isSuccess) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                RmcFilledButton(
                    value = "Go to login",
                    onClick = {
                        navigateToScreen(RmcScreen.Login.name)
                    }
                )
            },
            title = { Text("Registration successful") },
            text = { Text("Your account has been created successfully.") }
        )
    }

    uiState.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = {
                viewModel.onEvent(RegisterUIEvent.ErrorShown)
            },
            confirmButton = {
                RmcFilledButton(
                    value = "OK",
                    onClick = {
                        viewModel.onEvent(RegisterUIEvent.ErrorShown)
                    }
                )
            },
            title = { Text("Error") },
            text = { Text(error) }
        )
    }

    Scaffold(
        topBar = {
            RmcAppBar(
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
            Box(modifier = Modifier.fillMaxSize()) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = dimensionResource(R.dimen.padding_large))
                        .verticalScroll(rememberScrollState())
                ) {

                    RmcTextField(
                        label = stringResource(R.string.email),
                        leadingIcon = Icons.Filled.Email,
                        value = uiState.email,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        onValueChange = {
                            viewModel.onEvent(RegisterUIEvent.EmailChanged(it))
                        }
                    )

                    RmcSpacer(8)

                    RmcTextField(
                        label = stringResource(R.string.password),
                        leadingIcon = Icons.Filled.Lock,
                        value = uiState.password,
                        isPassword = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        onValueChange = {
                            viewModel.onEvent(RegisterUIEvent.PasswordChanged(it))
                        }
                    )

                    RmcSpacer(8)

                    RmcTextField(
                        label = "Naam",
                        value = uiState.fullName,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        onValueChange = {
                            viewModel.onEvent(RegisterUIEvent.FullNameChanged(it))
                        }
                    )

                    RmcSpacer(8)

                    RmcTextField(
                        label = "Telefoonnummer",
                        value = uiState.phone,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        onValueChange = {
                            viewModel.onEvent(RegisterUIEvent.PhoneChanged(it))
                        }
                    )

                    RmcSpacer(8)

                    RmcTextField(
                        label = "Adres",
                        value = uiState.address,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        onValueChange = {
                            viewModel.onEvent(RegisterUIEvent.AddressChanged(it))
                        }
                    )

                    RmcSpacer(8)

                    RmcTextField(
                        label = "Postcode",
                        value = uiState.zipcode,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        onValueChange = {
                            viewModel.onEvent(RegisterUIEvent.ZipcodeChanged(it))
                        }
                    )

                    RmcSpacer(8)

                    RmcTextField(
                        label = "Stad",
                        value = uiState.city,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        onValueChange = {
                            viewModel.onEvent(RegisterUIEvent.CityChanged(it))
                        }
                    )

                    RmcSpacer(8)

                    RmcTextField(
                        label = "Landcode (bv. NL)",
                        value = uiState.countryISO,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        onValueChange = {
                            viewModel.onEvent(RegisterUIEvent.CountryISOChanged(it))
                        }
                    )

                    RmcSpacer(16)

                    RmcFilledButton(
                        value = stringResource(R.string.register),
                        isEnabled = !uiState.isLoading,
                        onClick = {
                            viewModel.onEvent(RegisterUIEvent.RegisterButtonClicked)
                        }
                    )

                    DividerTextComponent()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ClickableLoginTextComponent(
                            tryingToLogin = true,
                            onTextSelected = {
                                navigateToScreen(RmcScreen.Login.name)
                            }
                        )
                    }
                }

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}