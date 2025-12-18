package com.profgroep8.rmc_app.presentation.screens.CarInformation

import RmcFilledButton
import RmcScreen
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.network.models.domain.Car
import com.example.network.models.domain.CarFuelType
import com.profgroep8.rmc_app.R
import com.profgroep8.rmc_app.presentation.components.CarInfoItem
import com.profgroep8.rmc_app.presentation.components.RmcSpacer

@Preview
@Composable
fun CarInformationScreenPreview() {
    CarInformationScreen (
        car = Car(
            carID = 1,
            licensePlate = "AB-123-CD",
            brand = "Toyota",
            model = "Corolla",
            year = 2020,
            fuelType = CarFuelType.Diesel,
            userID = 1,
            price = 20000
        ),
        navigateToScreen = { string -> println(string)}
    )
}


@Composable
fun CarInformationScreen(
    car: Car?,
    navigateToScreen: (route: String) -> Unit
) {
    if (car == null) {
        navigateToScreen(RmcScreen.Home.name);
        return;
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {

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
                }
            )



            CarInfoItem(
                label = stringResource(R.string.license_plate),
                value = car.licensePlate
            )

            CarInfoItem(
                label = stringResource(R.string.brand),
                value = car.brand
            )

            CarInfoItem(
                label = stringResource(R.string.model),
                value = car.model
            )

            CarInfoItem(
                label = stringResource(R.string.year),
                value = car.year.toString()
            )

            CarInfoItem(
                label = stringResource(R.string.fuelType),
                value = car.fuelType.displayName
            )
            RmcSpacer(height = 24)

            CarImageUpload(
                imageUri = null,
                onUploadClick = {
                    // launch image picker here
                },
                onDeleteClick = {

                }
            )

            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
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
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.edit_car)) },
                    onClick = {
                        expanded = false
                        onEditClick()
                    }
                )
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
    imageUri: Uri?,
    onUploadClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { if (imageUri == null) onUploadClick() }
    ) {

        if (imageUri == null) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
//                Icon(
//                    imageVector = Icons.Default.Upload,
//                    contentDescription = null,
//                    modifier = Modifier.size(48.dp)
//                )
                Text(text = stringResource(R.string.upload_car_image))
            }
        } else {
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete image"
                )
            }
        }
    }
}



