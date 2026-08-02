package com.noteflow.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Strictly black/white/gray — no purple, no Material You dynamic wallpaper colors.
 * Per explicit request: the accent (primary) is white in light theme and black in
 * dark theme; every other role is overridden too, since Compose's lightColorScheme()/
 * darkColorScheme() otherwise fall back to Material3's default purple-tinted palette
 * for everything you don't explicitly set (which is why purple kept showing up even
 * after only overriding "primary").
 */
private val LightColors = lightColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFFEDEDED),
    onPrimaryContainer = Color.Black,
    secondary = Color.White,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFEDEDED),
    onSecondaryContainer = Color.Black,
    tertiary = Color.White,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFFEDEDED),
    onTertiaryContainer = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF2F2F2),
    onSurfaceVariant = Color(0xFF444444),
    outline = Color(0xFF767676)
)

private val DarkColors = darkColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2A2A2A),
    onPrimaryContainer = Color.White,
    secondary = Color.Black,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2A2A2A),
    onSecondaryContainer = Color.White,
    tertiary = Color.Black,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF2A2A2A),
    onTertiaryContainer = Color.White,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF121212),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFCCCCCC),
    outline = Color(0xFF9E9E9E)
)

/** Lets any screen check whether dark theme is active, e.g. to skip per-note background colors. */
val LocalIsDarkTheme = staticCompositionLocalOf { false }

@Composable
fun NoteFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // No dynamicColor parameter/branch anymore: Material You would otherwise pull
    // accent colors straight from the device wallpaper, undoing this monochrome
    // palette on Android 12+ regardless of what's defined above.
    val colorScheme = if (darkTheme) DarkColors else LightColors
    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}
