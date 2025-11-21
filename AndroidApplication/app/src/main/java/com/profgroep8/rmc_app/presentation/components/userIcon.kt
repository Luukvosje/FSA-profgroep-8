package com.profgroep8.rmc_app.presentation.components

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


/**
 * Composable that displays User icon
 */
@Composable
fun RmcUserIcon(
    imageSrc: String?,
    modifier: Modifier = Modifier,
    size: Dp,
    onClick: () -> Unit,
    editEnabled: Boolean = false
) {
    val imageUrl = if (imageSrc.isNullOrBlank()) {
        null // Set to null for AsyncImage to display default icon
    } else {
        imageSrc
    }
    Log.d("RmcUserIcon", "Loading image from URL: $imageUrl")

    Box {

        AsyncImage(
            model = ImageRequest.Builder(context = LocalContext.current).data(imageUrl)
                .crossfade(true).build(),
            error = painterResource(R.drawable.loading_img),
            placeholder = painterResource(R.drawable.usericon),
            contentDescription = stringResource(R.string.profile_picture),
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { onClick() }
        )
        if (editEnabled) {
            RmcFilledTonalIconButton(
                icon = Icons.Filled.Edit,
                label = R.string.camera,
                onClick = onClick
            )
        }
    }
}