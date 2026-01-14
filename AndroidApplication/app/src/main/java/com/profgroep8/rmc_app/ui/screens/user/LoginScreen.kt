package com.profgroep8.rmc_app.ui.screens.user

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.ui.components.ClickableLoginTextComponent
import com.profgroep8.rmc_app.ui.components.DividerTextComponent
import com.profgroep8.rmc_app.ui.components.RmcAppBar
import com.profgroep8.rmc_app.ui.components.RmcSpacer
import com.profgroep8.rmc_app.ui.components.RmcTextField
import com.profgroep8.rmc_app.ui.events.LoginUIEvent
import com.profgroep8.rmc_app.viewmodel.user.LoginViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    navigateToScreen: (String) -> Unit,
    navigateBack: () -> Unit = { navigateToScreen(RmcScreen.Welcome.name) }
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler { navigateBack() }

    LaunchedEffect(Unit) {
        snapshotFlow { uiState.isSuccess }
            .collect { success ->
                if (success) {
                    navigateToScreen(RmcScreen.Home.name)
                }
            }
    }

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