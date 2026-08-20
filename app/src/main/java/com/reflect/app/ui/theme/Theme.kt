package com.reflect.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = Accent,
    onPrimary = PureWhite,
    primaryContainer = AccentSoft,
    onPrimaryContainer = Accent,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnBackground,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = OnSurfaceVariant,
    error = Danger,
    errorContainer = DangerSoft,
    outline = DividerColor
)

@Composable
fun ReflectTheme(
    content: @Composable () -> Unit
) {
    // Dark-first, always. Calm by design — no light theme branch.
    MaterialTheme(
        colorScheme = DarkColors,
        typography = ReflectTypography,
        content = content
    )
}
