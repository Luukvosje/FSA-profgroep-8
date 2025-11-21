package com.profgroep8.rmc_app.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


/**
 * Stateless CheckboxComponent which consists out of a CheckBox and HyperLinkTextComponent
 */
@Composable
fun CheckboxComponent(
    value: String,
    onTextSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var checkedState by remember { mutableStateOf(false) }
        Checkbox(checked = checkedState,
            onCheckedChange = { checkedState = !checkedState }
        )
        HyperlinkTextComponent(value = value, onTextSelected)
    }
}
