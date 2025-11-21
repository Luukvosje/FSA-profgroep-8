import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp

/**
 * Default divider
 */
@Composable
fun RmcDivider() {
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.padding_medium)),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}
