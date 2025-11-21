package com.profgroep8.rmc_app.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Composable that displays a Text component with a specific styling for NormalText
 */
@Composable
fun NormalTextComponent(value: String) {
    Text(
        text = value,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp)
            .padding(bottom = 15.dp),
        style = MaterialTheme.typography.bodyLarge
    )
}