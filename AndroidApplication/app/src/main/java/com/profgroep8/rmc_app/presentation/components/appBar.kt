package com.profgroep8.rmc_app.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Composable that show the topBar with navigation and title
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RmcAppBar(
    @StringRes title: Int,
    navigationIcon: ImageVector,
    navigateUp: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            RmcOutlinedIconButton(
                icon = navigationIcon,
                label = title,
                onClick = navigateUp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        },
        title = {
            Text(
                text = stringResource(title),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge
            )
        }
    )
}
