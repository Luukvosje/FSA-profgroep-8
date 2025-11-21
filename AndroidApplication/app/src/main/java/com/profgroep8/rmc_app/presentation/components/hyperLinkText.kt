package com.profgroep8.rmc_app.presentation.components

import android.util.Log
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle


/**
 * Composable that displays a ClickableText for the Privacy Police and Terms of Use
 */
@Composable
fun HyperlinkTextComponent(
    value: String,
    onTextSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialText = "By continuing you accept our "
    val privacyPolicyText = "Privacy Policy"
    val andText = " and "
    val termsAndConditionsText = "Term of Use"

    val annotatedString = buildAnnotatedString {
        append(initialText)
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            pushStringAnnotation(tag = privacyPolicyText, annotation = privacyPolicyText)
            append(privacyPolicyText)
        }
        append(andText)
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            pushStringAnnotation(tag = termsAndConditionsText, annotation = termsAndConditionsText)
            append(termsAndConditionsText)
        }
    }

    ClickableText(text = annotatedString, onClick = { offset ->

        annotatedString.getStringAnnotations(offset, offset)
            .firstOrNull()?.also { span ->
                Log.d("ClickableTextComponent", "{$span}")

                if (span.item == termsAndConditionsText || span.item == privacyPolicyText) {
                    onTextSelected(span.item)
                }

            }
    })
}