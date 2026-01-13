package com.profgroep8.rmc_app.ui.screens.userinfo

import RmcScreen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.network.models.domain.User
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.ui.components.RmcAppBar
import com.profgroep8.rmc_app.ui.components.RmcSpacer
import com.profgroep8.rmc_app.ui.components.UserInfoItem
import com.profgroep8.rmc_app.viewmodel.user.UserInfoViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UserInfoScreen(
    navigateToScreen: (String) -> Unit,
    viewModel: UserInfoViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    UserInfoScreenUI(
        user = uiState.user,
        loading = uiState.isLoading,
        navigateToScreen = navigateToScreen,
        onRefresh = { viewModel.refreshUserInfo() }
    )
}

@Composable
fun UserInfoScreenUI(
    user: User?,
    loading: Boolean,
    navigateToScreen: (String) -> Unit,
    onRefresh: () -> Unit
) {
    Scaffold (
        topBar = {
            RmcAppBar (
                title = stringResource(R.string.user_information),
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                onNavigateUp = { navigateToScreen(RmcScreen.Home.name) }
            )
        }
    ) { innerPadding ->
    Surface(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.padding_large))
        ) {

            RmcSpacer(height = 24)

            if (loading && user == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    UserInfoItem(
                        label = stringResource(R.string.user),
                        value = user?.fullName,
                        loading = user == null
                    )

                    UserInfoItem(
                        label = stringResource(R.string.email),
                        value = user?.email,
                        loading = user == null
                    )

                    UserInfoItem(
                        label = stringResource(R.string.phone),
                        value = user?.phone,
                        loading = user == null
                    )

                    UserInfoItem(
                        label = stringResource(R.string.address),
                        value = user?.address,
                        loading = user == null
                    )

                    UserInfoItem(
                        label = stringResource(R.string.zipcode),
                        value = user?.zipcode,
                        loading = user == null
                    )

                    UserInfoItem(
                        label = stringResource(R.string.city),
                        value = user?.city,
                        loading = user == null
                    )

                    UserInfoItem(
                        label = stringResource(R.string.country),
                        value = user?.countryISO,
                        loading = user == null
                    )

                    UserInfoItem(
                        label = stringResource(R.string.bonus_points),
                        value = user?.bonusPoints?.toString(),
                        loading = user == null
                    )
                }
            }
        }
    }
}

@Composable
fun UserInfoTopBar(
    title: String,
    onRefreshClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )

        IconButton(onClick = onRefreshClick) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh user info"
            )
        }
    }
}
}