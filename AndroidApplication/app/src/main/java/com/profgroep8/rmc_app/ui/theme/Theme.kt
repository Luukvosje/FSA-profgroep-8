package com.profgroep8.rmc_app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ---- COLOR SCHEMES ----

private val DarkColorScheme = darkColorScheme(
    primary = RedPrimary,
    onPrimary = Color.White,

    secondary = AccentOrange,
    onSecondary = Color.White,

    tertiary = RedPrimaryLight,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),

    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = RedPrimary,
    onPrimary = Color.White,

    secondary = AccentOrange,
    onSecondary = Color.White,

    tertiary = RedPrimaryLight,

    background = Background,
    surface = SurfaceWhite,

    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

@Composable
fun RMCappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
