package com.profgroep8.rmc_app.ui.screens.login

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.ui.components.*
import com.profgroep8.rmc_app.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    navigateToScreen: (String) -> Unit,
    navigateBack: () -> Unit = { navigateToScreen(RmcScreen.Welcome.name) }
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler { navigateBack() }

    // ✅ Navigate EXACTLY ONCE after successful login (token already stored)
    LaunchedEffect(Unit) {
        snapshotFlow { uiState.isSuccess }
            .collect { success ->
                if (success) {
                    navigateToScreen(RmcScreen.Home.name)
                }
            }
    }

    // ❌ Error dialog
    uiState.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = {
                viewModel.onEvent(LoginUIEvent.ErrorShown)
            },
            confirmButton = {
                RmcFilledButton(
                    value = "OK",
                    onClick = {
                        viewModel.onEvent(LoginUIEvent.ErrorShown)
                    }
                )
            },
            title = { Text("Login failed") },
            text = { Text(error) }
        )
    }

    Scaffold(
        topBar = {
            RmcAppBar(
                title = stringResource(R.string.login),
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
                            viewModel.onEvent(LoginUIEvent.EmailChanged(it))
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
                            imeAction = ImeAction.Done
                        ),
                        onValueChange = {
                            viewModel.onEvent(LoginUIEvent.PasswordChanged(it))
                        }
                    )

                    RmcSpacer(16)

                    RmcFilledButton(
                        value = stringResource(R.string.login),
                        isEnabled = !uiState.isLoading,
                        onClick = {
                            viewModel.onEvent(LoginUIEvent.LoginButtonClicked)
                        }
                    )

                    DividerTextComponent()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ClickableLoginTextComponent(
                            tryingToLogin = false,
                            onTextSelected = {
                                navigateToScreen(RmcScreen.Register.name)
                            }
                        )
                    }
                }

                // ⏳ Loading overlay
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