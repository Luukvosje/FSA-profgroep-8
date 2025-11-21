package com.profgroep8.rmc_app.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * Composable that displays RENT MY CAR in text
 */
@Composable
fun RmcLogoText() {
    androidx.compose.material.Text(
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Black)) {
                append("RENT ")
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    append("MY")
                }
                append(" CAR")
            }
        },
        style = MaterialTheme.typography.displayLarge,
        color = colorResource(id = R.color.primary_red)
    )
}

