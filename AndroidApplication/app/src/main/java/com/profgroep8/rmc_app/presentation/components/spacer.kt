package com.profgroep8.rmc_app.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.profgroep8.rmc_app.R

@Composable
fun RmcSpacer(height: Int = 24) {
    Spacer(modifier = Modifier.height(height.dp))
}

@Composable
fun DividerTextComponent() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(id = R.dimen.padding_medium)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            thickness = 1.dp,
            color = Color.Gray
        )
        Text(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small)),
            text = stringResource(R.string.or),
            fontSize = 18.sp,
            color = Color.Gray
        )
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            thickness = 1.dp,
            color = Color.Gray
        )
    }
}

