package com.profgroep8.rmc_app.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Default spacer
 */
@Composable
fun RmcSpacer(height: Int = 24) {
    Spacer(modifier = Modifier.height(height.dp))
}
