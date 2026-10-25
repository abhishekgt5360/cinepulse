package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode { CRIMSON, OLED, DYNAMIC, HIGH_CONTRAST, PAPER }

val LocalHighContrast = staticCompositionLocalOf { false }

val CrimsonScheme = darkColorScheme(
    primary = CrimsonPrimary,
    onPrimary = CrimsonOnPrimary,
    primaryContainer = CrimsonPrimaryContainer,
    onPrimaryContainer = CrimsonOnPrimaryContainer,
    secondary = CrimsonSecondary,
    onSecondary = CrimsonOnSecondary,
    secondaryContainer = CrimsonSecondaryContainer,
    onSecondaryContainer = CrimsonOnSecondaryContainer,
    tertiary = CrimsonTertiary,
    onTertiary = CrimsonOnTertiary,
    tertiaryContainer = CrimsonTertiaryContainer,
    onTertiaryContainer = CrimsonOnTertiaryContainer,
    background = CrimsonBackground,
    onBackground = CrimsonOnBackground,
    surface = CrimsonSurface,
    onSurface = CrimsonOnSurface,
    surfaceVariant = CrimsonSurfaceVariant,
    onSurfaceVariant = CrimsonOnSurfaceVariant,
    outline = CrimsonOutline,
    outlineVariant = CrimsonOutlineVariant,
    surfaceContainerLowest = CrimsonSurfaceContainerLowest,
    surfaceContainerLow = CrimsonSurfaceContainerLow,
    surfaceContainer = CrimsonSurfaceContainer,
    surfaceContainerHigh = CrimsonSurfaceContainerHigh,
    surfaceContainerHighest = CrimsonSurfaceContainerHighest,
    error = CrimsonError,
    onError = CrimsonOnError,
    errorContainer = CrimsonErrorContainer,
    onErrorContainer = CrimsonOnErrorContainer
)

val OledScheme = darkColorScheme(
    primary = OledPrimary,
    onPrimary = OledOnPrimary,
    primaryContainer = OledPrimaryContainer,
    onPrimaryContainer = OledOnPrimaryContainer,
    secondary = OledSecondary,
    onSecondary = OledOnSecondary,
    secondaryContainer = OledSecondaryContainer,
    onSecondaryContainer = OledOnSecondaryContainer,
    tertiary = OledTertiary,
    onTertiary = OledOnTertiary,
    tertiaryContainer = OledTertiaryContainer,
    onTertiaryContainer = OledOnTertiaryContainer,
    background = OledBackground,
    onBackground = OledOnBackground,
    surface = OledSurface,
    onSurface = OledOnSurface,
    surfaceVariant = OledSurfaceVariant,
    onSurfaceVariant = OledOnSurfaceVariant,
    outline = OledOutline,
    outlineVariant = OledOutlineVariant,
    surfaceContainerLowest = OledSurfaceContainerLowest,
    surfaceContainerLow = OledSurfaceContainerLow,
    surfaceContainer = OledSurfaceContainer,
    surfaceContainerHigh = OledSurfaceContainerHigh,
    surfaceContainerHighest = OledSurfaceContainerHighest,
    error = CrimsonError,
    onError = CrimsonOnError,
    errorContainer = CrimsonErrorContainer,
    onErrorContainer = CrimsonOnErrorContainer
)

val HighContrastScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFFFFFFFF),
    onPrimaryContainer = Color(0xFF000000),
    secondary = Color(0xFFE0E0E0),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFFFFFFFF),
    onSecondaryContainer = Color(0xFF000000),
    tertiary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFFFFFFFF),
    onTertiaryContainer = Color(0xFF000000),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF0D0D0D),
    onSurfaceVariant = Color(0xFFE0E0E0),
    surfaceTint = Color(0xFF000000),
    surfaceDim = Color(0xFF000000),
    surfaceBright = Color(0xFF1A1A1A),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF000000),
    surfaceContainer = Color(0xFF000000),
    surfaceContainerHigh = Color(0xFF0D0D0D),
    surfaceContainerHighest = Color(0xFF141414),
    outline = Color(0xFF8C8C8C),
    outlineVariant = Color(0xFF8C8C8C),
    inverseSurface = Color(0xFFFFFFFF),
    inverseOnSurface = Color(0xFF000000),
    inversePrimary = Color(0xFF000000),
    scrim = Color(0xFF000000),
    error = Color(0xFFFF8A80),
    onError = Color(0xFF000000),
    errorContainer = Color(0xFFFF8A80),
    onErrorContainer = Color(0xFF000000)
)

val PaperScheme = lightColorScheme(
    primary = Color(0xFF1A1A1A),
    onPrimary = Color(0xFFFAF7F2),
    primaryContainer = Color(0xFFDDD7CA),
    onPrimaryContainer = Color(0xFF1A1A1A),
    secondary = Color(0xFF55524A),
    onSecondary = Color(0xFFFAF7F2),
    secondaryContainer = Color(0xFFDDD7CA),
    onSecondaryContainer = Color(0xFF1A1A1A),
    tertiary = Color(0xFF3A3A3A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD4CFC2),
    onTertiaryContainer = Color(0xFF1A1A1A),
    background = Color(0xFFFAF7F2),
    onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFFAF7F2),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFE6E1D7),
    onSurfaceVariant = Color(0xFF55524A),
    surfaceTint = Color(0xFFFAF7F2),
    surfaceDim = Color(0xFFDDD8CE),
    surfaceBright = Color(0xFFFAF7F2),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF5F1EA),
    surfaceContainer = Color(0xFFF0ECE4),
    surfaceContainerHigh = Color(0xFFE6E1D7),
    surfaceContainerHighest = Color(0xFFDDD7CA),
    outline = Color(0xFF7D776B),
    outlineVariant = Color(0xFFCFC8BA),
    inverseSurface = Color(0xFF2A2925),
    inverseOnSurface = Color(0xFFF5F1EA),
    inversePrimary = Color(0xFFE6E1D7),
    scrim = Color(0xFF000000),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B)
)

@Composable
fun CinePulseTheme(
    mode: ThemeMode = ThemeMode.CRIMSON,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    val colorScheme = when (mode) {
        ThemeMode.DYNAMIC -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dynamicDarkColorScheme(context)
            } else {
                CrimsonScheme
            }
        }
        ThemeMode.OLED -> OledScheme
        ThemeMode.HIGH_CONTRAST -> HighContrastScheme
        ThemeMode.PAPER -> PaperScheme
        ThemeMode.CRIMSON -> CrimsonScheme
    }

    val isLightTheme = mode == ThemeMode.PAPER

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = 0x00000000
            @Suppress("DEPRECATION")
            window.navigationBarColor = 0x00000000
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = isLightTheme
            insetsController.isAppearanceLightNavigationBars = isLightTheme
        }
    }

    CompositionLocalProvider(LocalHighContrast provides (mode == ThemeMode.HIGH_CONTRAST)) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
