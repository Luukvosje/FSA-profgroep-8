package com.profgroep8.rmc_app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation


@Composable
fun RmcTextField(
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconButtonClick: () -> Unit = {},
    placeholder: String? = null,
    isPassword: Boolean = false,
    readOnly: Boolean = false,
    maxLines: Int = 1,
    value: String = "",
    isError: Boolean = false,
    enabled: Boolean = true,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyLarge,
        singleLine = maxLines == 1,
        maxLines = maxLines,
        readOnly = readOnly,
        enabled = enabled,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        label = {
            Text(
                text = label,
                style = if (isError) MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.error
                ) else MaterialTheme.typography.bodyLarge
            )
        },
        leadingIcon = leadingIcon?.let {
            { Icon(imageVector = leadingIcon, contentDescription = null) }
        },
        trailingIcon = trailingIcon?.let {
            {
                IconButton(onClick = onTrailingIconButtonClick) {
                    Icon(imageVector = trailingIcon, contentDescription = null)
                }
            }
        },
        placeholder = placeholder?.let { { Text(text = it, color = MaterialTheme.colorScheme.primary) } },
        colors = OutlinedTextFieldDefaults.colors(
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.primary,
            errorContainerColor = MaterialTheme.colorScheme.error,
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorTextColor = MaterialTheme.colorScheme.error,
            unfocusedTextColor = MaterialTheme.colorScheme.inversePrimary,
            focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
            disabledBorderColor = if(readOnly && !enabled) MaterialTheme.colorScheme.primary else Color.Unspecified,
            disabledLabelColor = if(readOnly && !enabled) MaterialTheme.colorScheme.primary else Color.Unspecified,
        )
    )
}

