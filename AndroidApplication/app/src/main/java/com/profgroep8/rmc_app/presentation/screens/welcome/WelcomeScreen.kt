package com.digitalarchitects.rmc_app.presentation.screens.welcome

import LogoComponent
import RmcFilledButton
import RmcFilledTonalButton
import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.presentation.components.RmcLogoText
import com.profgroep8.rmc_app.presentation.components.RmcSpacer
import com.profgroep8.rmc_app.presentation.screens.welcome.WelcomeViewModel
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
fun WelcomeScreen(
    viewModel: WelcomeViewModel,
    navigateToScreen: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    fun showToast(context: Context, @StringRes messageResId: Int) {
        Toast.makeText(context, context.getString(messageResId), Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(viewModel, context) {
//        viewModel.authResult.collect { result ->
//            val messageResId = when (result) {
//                is AuthResult.Authorized -> {
//                    viewModel.getDataFromRemoteSource()
//                    navigateToScreen(RmcScreen.RentACar.name)
//                    return@collect
//                }
//                is AuthResult.Unauthorized -> R.string.toast_unauthorized
//                is AuthResult.NoConnectionError -> R.string.toast_no_connection
//                is AuthResult.UnknownError -> R.string.toast_unknown_error
//            }
//
//            showToast(context, messageResId)
//        }
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

                RmcSpacer(8)
            Column(Modifier.weight(2f), verticalArrangement = Arrangement.Center,){
                RmcFilledTonalButton(
                    value = stringResource(id = R.string.register),
                    onClick = { navigateToScreen(RmcScreen.Register.name) }
                )
                RmcFilledButton(
                    value = stringResource(id = R.string.login),
                    onClick = { navigateToScreen(RmcScreen.Login.name) }
                )
            }
            }

        }
    }

}