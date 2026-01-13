package com.profgroep8.rmc_app.ui.screens.car

import PhotoUtils
import RmcFilledButton
import RmcScreen
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.network.models.domain.Car
import com.example.network.services.UserProvider
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.ui.components.CarInfoItem
import com.profgroep8.rmc_app.ui.components.RmcSpacer
import com.profgroep8.rmc_app.utils.PhotoPermissionHandler
import com.profgroep8.rmc_app.utils.rememberPhotoPermissionLauncher
import com.profgroep8.rmc_app.viewmodel.car.CarInformationUiState
import com.profgroep8.rmc_app.viewmodel.car.CarInformationViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CarInformationScreen(
    carId: Int?,
    navigateToScreen: (route: String) -> Unit,
    available: Boolean?,
    viewModel: CarInformationViewModel = koinViewModel(parameters = { parametersOf(carId) }),
){
    if (carId == null) {
        navigateToScreen(RmcScreen.Home.name);
        return;
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val loading by viewModel.isLoading.collectAsStateWithLifecycle()

    CarInformationScreenUI(
        car=uiState.car,
        navigateToScreen = navigateToScreen,
        loading = loading,
        uistate = uiState,
        viewModel = viewModel,
        available = available?: false
    )
}

@Composable
fun CarInformationScreenUI(
    car: Car?,
    loading: Boolean,
    navigateToScreen: (route: String)-> Unit,
    uistate: CarInformationUiState,
    viewModel: CarInformationViewModel,
    available: Boolean = false
) {
    val context = LocalContext.current
    val userId = UserProvider.user?.userID
    val isOwner = userId != null && car != null && userId == car.userID

    val photoPermissionHandler = remember {
        PhotoPermissionHandler(
            context = context,
            onPermissionsResult = { granted ->
                viewModel.onPhotoPermissionResult(granted)
            }
        )
    }

    val photoPermissionLauncher = rememberPhotoPermissionLauncher { granted ->
        viewModel.onPhotoPermissionResult(granted)
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        viewModel.addPhoto(uri.toString(), context)
    }

    var currentCameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentCameraUri?.let { uri ->
                viewModel.addPhoto(uri.toString(), context)
            }
        }
        currentCameraUri = null
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        CarInformationDialogs(
            uiState = uistate,
            onDismissImageSource = { viewModel.dismissImageSourceDialog() },
            onCameraClick = {
                PhotoUtils.createImageUri(context)?.let { uri ->
                    currentCameraUri = uri
                    cameraLauncher.launch(uri)
                }
            },
            onGalleryClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onDismissPermissionWarning = { viewModel.dismissPermissionWarning() },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.padding_large))
        ) {
            CarInformationTopBar(
                title = stringResource(R.string.car_information),
                onEditClick = {
//                    navigateToScreen(RmcScreen.EditCar.name)
                },
                onDeleteClick = {
//                    navigateToScreen(RmcScreen.DeleteCar.name)
                    viewModel.deleteCar(navigateToScreen)
                }
            )



        CarInfoItem(
            label = stringResource(R.string.license_plate),
            value = car?.licensePlate,
            loading = car == null
        )

        CarInfoItem(
            label = stringResource(R.string.brand),
            value = car?.brand,
            loading = car == null
        )

        CarInfoItem(
            label = stringResource(R.string.model),
            value = car?.model,
            loading = car == null
        )

        CarInfoItem(
            label = stringResource(R.string.year),
            value = car?.year.toString(),
            loading = car == null
        )

        CarInfoItem(
            label = stringResource(R.string.fuelType),
            value =  car?.fuelType?.displayName,
            loading = car == null
        )
            RmcSpacer(height = 24)

            CarImageUpload(
                imageBytes = car?.imageBytes,
                onUploadClick = {
                    if (photoPermissionHandler.hasPermissions()) {
                        viewModel.showImageSourceDialog()
                    } else {
                        photoPermissionHandler.requestPermissions(photoPermissionLauncher)
                    }
                },
                isLoading = uistate.isImageLoading,
                onDeleteClick = { viewModel.deleteCarImage() }
            )

            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
                if (car != null && !isOwner && available) {
                    RmcFilledButton(
                        value = stringResource(R.string.create_rental),
                        onClick = { navigateToScreen("${RmcScreen.AddRental.name}/${car.carID}") }
                    )
                    RmcSpacer(height = 16)
                }
                RmcFilledButton(
                    value = stringResource(R.string.button_back),
                    onClick = { navigateToScreen(RmcScreen.Home.name) }
                )
            }

        }
    }
}

@Composable
fun CarInformationTopBar(
    title: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )

        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options"
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
//                DropdownMenuItem(
//                    text = { Text(stringResource(R.string.edit_car)) },
//                    onClick = {
//                        expanded = false
//                        onEditClick()
//                    }
//                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.delete_car)) },
                    onClick = {
                        expanded = false
                        onDeleteClick()
                    }
                )
            }
        }
    }
}

@Composable
fun CarImageUpload(
    imageBytes: ByteArray?,
    isLoading: Boolean,
    onUploadClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { if (imageBytes == null) onUploadClick() },
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator()
            }

            imageBytes == null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FileUpload, null, Modifier.size(48.dp))
                    Text(stringResource(R.string.upload_car_image))
                }
            }

            else -> {
                AsyncImage(
                    model = imageBytes,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                ) {
                    Icon(Icons.Default.Delete, null)
                }
            }
        }
    }
}


@Composable
private fun CarInformationDialogs(
    uiState: CarInformationUiState,
    onDismissImageSource: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onDismissPermissionWarning: () -> Unit,
) {
    if (uiState.showImageSourceDialog) {
        ImageSourceDialog(
            onDismiss = onDismissImageSource,
            onCameraClick = {
                onDismissImageSource()
                onCameraClick()
            },
            onGalleryClick = {
                onDismissImageSource()
                onGalleryClick()
            }
        )
    }

    if (uiState.showPermissionDeniedWarning) {
        PermissionDeniedDialog(
            onDismiss = onDismissPermissionWarning
        )
    }
}


@Composable
fun ImageSourceDialog(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.add_photo),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                RmcFilledButton(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = stringResource(R.string.take_photo),
                    onClick = onCameraClick
                )

                RmcFilledButton(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = stringResource(R.string.choose_from_gallery),
                    onClick = onGalleryClick
                )

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        }
    }
}

@Composable
fun PermissionDeniedDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFFF59E0B)
                )

                Text(
                    text = stringResource(R.string.permission_required),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(R.string.to_add_photos_please_grant_camera_and_storage_permissions_in_your_device_settings),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                RmcFilledButton(
                    modifier = Modifier
                    .fillMaxWidth(),
                    value = stringResource(R.string.button_ok),
                    onClick = onDismiss
                )
            }
        }
    }
}



