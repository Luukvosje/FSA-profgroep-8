import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun RmcFilledButton(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    color: Color = MaterialTheme.colorScheme.primary,
    value: String,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = { onClick() },
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(color),
        modifier = modifier
            .fillMaxWidth()
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun RmcFilledTonalButton(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    value: String,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = { onClick() },
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun RmcOutlinedButton(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    value: String,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = { onClick() },
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun RmcFilledIconButton(
    icon: ImageVector,
    @StringRes label: Int,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier
) {
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(label),
        )
    }
}

@Composable
fun RmcFilledTonalIconButton(
    icon: ImageVector,
    @StringRes label: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    FilledTonalIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        ),
        enabled = enabled,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(label),
        )
    }
}

@Composable
fun RmcOutlinedIconButton(
    icon: ImageVector,
    @StringRes label: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedIconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(label)
        )
    }
}

@Composable
fun RmcImgFilledIconButton(
    profileImageSrc: String?,
    @StringRes label: Int,
    onClick: () -> Unit,
    modifier: Modifier
) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        val imageUrl = if (profileImageSrc.isNullOrBlank()) {
            null // Set to null for AsyncImage to display default icon
        } else {
            profileImageSrc
        }

        AsyncImage(
            model = ImageRequest.Builder(context = LocalContext.current).data(imageUrl)
                .crossfade(true).build(),
            error = painterResource(R.drawable.loading_img),
            placeholder = painterResource(R.drawable.usericon),
            contentDescription = stringResource(R.string.profile_picture),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
        )
    }
}

@Composable
fun RmcBadge(value: String) {
    Badge {
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun RmcFloatingActionButton(
    icon: ImageVector,
    @StringRes label: Int,
    modifier: Modifier = Modifier,
    number: Int? = null,
    onClick: () -> Unit
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp
        ),
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            if (number == null) {
                Text(
                    text = stringResource(label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = stringResource(label, number),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

        },
    )
}