package com.profgroep8.rmc_app.presentation.components

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation


/**
 * Text field composable
 */
@Composable
fun RmcTextField(
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconButtonClick: () -> Unit = {},
    placeholder: String? = null,
    isPassword: Boolean = false,
    maxLines: Int = 1,
    value: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.None
    )
) {
    OutlinedTextField(
        shape = MaterialTheme.shapes.small,
        label = {
            Text(
                text = label,
                style = if (isError) MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.error
                ) else MaterialTheme.typography.bodyLarge,
            )
        },
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = OutlinedTextFieldDefaults.colors(
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.primary,
            errorContainerColor = MaterialTheme.colorScheme.error,
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorTextColor = MaterialTheme.colorScheme.error,
            unfocusedTextColor = MaterialTheme.colorScheme.scrim,
            focusedTrailingIconColor = MaterialTheme.colorScheme.primary
        ),
        keyboardOptions = keyboardOptions,
        modifier = modifier.fillMaxWidth(),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        maxLines = maxLines,
        singleLine = maxLines == 1,
        value = value ?: "",
        enabled = enabled,
        onValueChange = onValueChange,
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null
                )
            }
        },
        trailingIcon = trailingIcon?.let {
            {
                IconButton(onClick = {
                    onTrailingIconButtonClick()
                }) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null
                    )
                }
            }
        },
        placeholder = placeholder?.let {
            {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}
