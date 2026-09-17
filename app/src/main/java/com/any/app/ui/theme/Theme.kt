package com.any.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = AnyDarkAccent,
    onPrimary = AnyDarkBackground,
    background = AnyDarkBackground,
    onBackground = AnyDarkText,
    surface = AnyDarkSurface,
    onSurface = AnyDarkText,
    surfaceVariant = AnyDarkSurfaceVariant,
    onSurfaceVariant = AnyDarkTextMuted
)

private val LightColorScheme = lightColorScheme(
    primary = AnyAccent,
    onPrimary = AnySurface,
    primaryContainer = AnyAccentContainer,
    onPrimaryContainer = AnyText,
    background = AnyBackground,
    onBackground = AnyText,
    surface = AnySurface,
    onSurface = AnyText,
    surfaceVariant = AnySurfaceVariant,
    onSurfaceVariant = AnyTextMuted,
    outline = AnyOutline
)

private val AnyShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp)
)

@Composable
fun AnyTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = AnyShapes,
        content = content
    )
}
