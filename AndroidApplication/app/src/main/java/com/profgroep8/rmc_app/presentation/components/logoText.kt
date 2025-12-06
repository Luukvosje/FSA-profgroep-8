package com.profgroep8.rmc_app.presentation.components

import android.util.Size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.profgroep8.rmc_app.R

@Composable
fun RmcLogoText() {
    Text(
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Black, fontSize = 50.sp)) {
                append("RENT ")
                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    append("MY")
                }
                append(" CAR")
            }
        },
        style = MaterialTheme.typography.displayLarge,
        color = colorResource(id = R.color.blue_500),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

