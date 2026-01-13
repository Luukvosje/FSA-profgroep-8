package com.profgroep8.rmc_app.ui.screens.rental

import RmcFilledButton
import RmcScreen
import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.ui.components.RmcAppBar
import com.profgroep8.rmc_app.ui.components.RmcSpacer
import com.profgroep8.rmc_app.viewmodel.rental.AddRentalUiState
import com.profgroep8.rmc_app.viewmodel.rental.AddRentalViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Preview(showBackground = true)
@Composable
fun AddRentalScreenPreview() {
    AddRentalScreen(
        carId = 1,
        navigateToScreen = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRentalScreen(
    carId: Int?,
    navigateToScreen: (String) -> Unit,
    viewModel: AddRentalViewModel = koinViewModel(parameters = { parametersOf(carId ?: 0) }),
    navigateBack: () -> Unit = { navigateToScreen(RmcScreen.Home.name) }
) {
    if (carId == null) {
        navigateToScreen(RmcScreen.Home.name)
        return
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val loading by viewModel.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (granted) {
            viewModel.getCurrentLocation(context, true)
        }
    }

    LaunchedEffect(uiState.startLatitude) {
        if (uiState.startLatitude == null && !uiState.isGettingLocation) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    if (uiState.needsLocationPermission) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLocationPermissionDialog() },
            title = { Text(stringResource(R.string.location_permission_required)) },
            text = { Text(stringResource(R.string.grant_location_permission)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.dismissLocationPermissionDialog()
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        )
                    }
                ) {
                    Text(stringResource(R.string.button_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissLocationPermissionDialog() }) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        )
    }

    if (uiState.showCarAlreadyRentedDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCarAlreadyRentedDialog() },
            title = { Text(stringResource(R.string.car_already_rented)) },
            text = { 
                Text(stringResource(R.string.car_already_rented_message))
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissCarAlreadyRentedDialog() }) {
                    Text(stringResource(R.string.button_ok))
                }
            }
        )
    }

    BackHandler {
        navigateBack()
    }

    Scaffold(
        topBar = {
            RmcAppBar(
                title = stringResource(R.string.create_rental),
                subtitle = stringResource(R.string.create_rental_sub),
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
            when {
                loading && uiState.car == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.errorMessage != null && uiState.car == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(dimensionResource(R.dimen.padding_large)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.errorMessage ?: stringResource(R.string.unknown_error),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        RmcSpacer(16)
                        TextButton(
                            onClick = { viewModel.retry() }
                        ) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
                else -> {
                    CreateRentalContent(
                        uiState = uiState,
                        loading = loading,
                        onStartDateChange = viewModel::onStartDateChange,
                        onStartTimeChange = viewModel::onStartTimeChange,
                        onEndDateChange = viewModel::onEndDateChange,
                        onEndTimeChange = viewModel::onEndTimeChange,
                        onEndAddressChange = viewModel::onEndAddressChange,
                        onSearchAddress = viewModel::searchAddress,
                        onCreateClick = { viewModel.createRental(navigateToScreen) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateRentalContent(
    uiState: AddRentalUiState,
    loading: Boolean,
    onStartDateChange: (Long?) -> Unit,
    onStartTimeChange: (String) -> Unit,
    onEndDateChange: (Long?) -> Unit,
    onEndTimeChange: (String) -> Unit,
    onEndAddressChange: (String) -> Unit,
    onSearchAddress: () -> Unit,
    onCreateClick: () -> Unit
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.startDate)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onStartDateChange(datePickerState.selectedDateMillis)
                    showStartDatePicker = false
                }) {
                    Text(stringResource(R.string.button_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.endDate)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onEndDateChange(datePickerState.selectedDateMillis)
                    showEndDatePicker = false
                }) {
                    Text(stringResource(R.string.button_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(dimensionResource(R.dimen.padding_large))
    ) {
        if (uiState.car != null) {
            Text(
                text = stringResource(R.string.car_information),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            RmcSpacer(16)

            CarInfoRow(
                label = stringResource(R.string.license_plate),
                value = uiState.car.licensePlate
            )
            CarInfoRow(
                label = stringResource(R.string.brand),
                value = uiState.car.brand
            )
            CarInfoRow(
                label = stringResource(R.string.model),
                value = uiState.car.model
            )
            CarInfoRow(
                label = stringResource(R.string.price),
                value = "€${uiState.car.price}"
            )

            RmcSpacer(24)
            HorizontalDivider()
            RmcSpacer(24)
        }

        Text(
            text = stringResource(R.string.rental_information),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        RmcSpacer(16)

        DatePickerField(
            label = stringResource(R.string.start_date),
            dateMillis = uiState.startDate,
            onClick = { showStartDatePicker = true }
        )

        RmcSpacer(8)

        FormTextField(
            label = stringResource(R.string.start_time),
            value = uiState.startTime,
            onValueChange = onStartTimeChange,
            error = uiState.startTimeError?.asString(),
            placeholder = stringResource(R.string.time_format_hint),
            keyboardType = KeyboardType.Number
        )

        RmcSpacer(8)

        LocationDisplay(
            label = stringResource(R.string.start_location_label),
            latitude = uiState.startLatitude,
            longitude = uiState.startLongitude,
            isGetting = uiState.isGettingLocation
        )

        RmcSpacer(16)

        DatePickerField(
            label = stringResource(R.string.end_date),
            dateMillis = uiState.endDate,
            onClick = { showEndDatePicker = true }
        )

        RmcSpacer(8)

        FormTextField(
            label = stringResource(R.string.end_time),
            value = uiState.endTime,
            onValueChange = onEndTimeChange,
            error = uiState.endTimeError?.asString(),
            placeholder = stringResource(R.string.time_format_hint),
            keyboardType = KeyboardType.Number
        )

        RmcSpacer(8)

        AddressSearchField(
            label = stringResource(R.string.end_address),
            value = uiState.endAddress,
            onValueChange = onEndAddressChange,
            onSearch = onSearchAddress,
            error = uiState.endAddressError?.asString(),
            placeholder = stringResource(R.string.address_hint)
        )

        RmcSpacer(8)

        if (uiState.endLatitude != null && uiState.endLongitude != null) {
            LocationDisplay(
                label = stringResource(R.string.end_location_label),
                latitude = uiState.endLatitude,
                longitude = uiState.endLongitude,
                isGetting = false
            )
        }

        if (uiState.errorMessage != null) {
            RmcSpacer(16)
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        RmcSpacer(24)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
        ) {
            RmcFilledButton(
                value = stringResource(R.string.button_create),
                isEnabled = uiState.isValid && !loading,
                onClick = onCreateClick
            )
        }
    }
}

@Composable
private fun CarInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    placeholder: String? = null,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it, style = MaterialTheme.typography.bodySmall) } },
            isError = error != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Next
            )
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

@Composable
private fun AddressSearchField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    error: String? = null,
    placeholder: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it, style = MaterialTheme.typography.bodySmall) } },
            isError = error != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { onSearch() }
            ),
            trailingIcon = {
                IconButton(onClick = onSearch) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search_address)
                    )
                }
            }
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

@Composable
private fun DatePickerField(
    label: String,
    dateMillis: Long?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd-MM-yyyy") }
    val displayText = if (dateMillis != null) {
        Instant.ofEpochMilli(dateMillis)
            .atZone(ZoneId.systemDefault())
            .format(dateFormatter)
    } else {
        ""
    }
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = stringResource(R.string.select_date)
                    )
                }
            }
        )
    }
}

@Composable
private fun LocationDisplay(
    label: String,
    latitude: Float?,
    longitude: Float?,
    isGetting: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        if (isGetting) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp),
                    strokeWidth = 2.dp
                )
                Text(
                    text = stringResource(R.string.getting_location),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else if (latitude != null && longitude != null) {
            Text(
                text = "${stringResource(R.string.current_location)}: ${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            Text(
                text = stringResource(R.string.getting_location),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

