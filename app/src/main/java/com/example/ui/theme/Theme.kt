package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode { CRIMSON, OLED, DYNAMIC }

private val CrimsonScheme = darkColorScheme(
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

private val OledScheme = darkColorScheme(
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

@Composable
fun CinePulseTheme(
    mode: ThemeMode = ThemeMode.CRIMSON,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    val colorScheme = when {
        mode == ThemeMode.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            dynamicDarkColorScheme(context)
        }
        mode == ThemeMode.OLED -> OledScheme
        else -> CrimsonScheme
    }

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
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
