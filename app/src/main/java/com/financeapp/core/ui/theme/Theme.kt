package com.financeapp.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.financeapp.core.domain.model.ColorScheme as AppColorScheme
import com.financeapp.core.domain.model.ThemeMode

private class Accent(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    val containerLight: Color,
    val containerDark: Color,
)

private val purple = Accent(PurplePrimary, PurpleSecondary, PurpleTertiary, PurpleGradStart, PurpleGradEnd, PurpleContainerLight, PurpleContainerDark)
private val orange = Accent(OrangePrimary, OrangeSecondary, OrangeTertiary, OrangeGradStart, OrangeGradEnd, OrangeContainerLight, OrangeContainerDark)

/** Accent colours exposed to composables that draw gradients / coloured depth (Soft Depth). */
data class AccentColors(val primary: Color, val gradientStart: Color, val gradientEnd: Color)

val LocalAccentColors = staticCompositionLocalOf {
    AccentColors(PurplePrimary, PurpleGradStart, PurpleGradEnd)
}

private fun lightScheme(a: Accent) = lightColorScheme(
    primary = a.primary, onPrimary = Color.White,
    secondary = a.secondary, onSecondary = Color.White,
    tertiary = a.tertiary, onTertiary = Color.White,
    primaryContainer = a.containerLight, onPrimaryContainer = a.primary,
    background = PaperLight, onBackground = InkLight,
    surface = SurfaceLight, onSurface = InkLight,
    surfaceVariant = SurfaceVariantLight, onSurfaceVariant = InkMutedLight,
    outline = LineLight, outlineVariant = LineLight,
    error = ExpenseRed, onError = Color.White,
)

private fun darkScheme(a: Accent) = darkColorScheme(
    primary = a.primary, onPrimary = Color.White,
    secondary = a.secondary, onSecondary = Color.White,
    tertiary = a.tertiary, onTertiary = Color.Black,
    primaryContainer = a.containerDark, onPrimaryContainer = Color.White,
    background = PaperDark, onBackground = InkDark,
    surface = SurfaceDark, onSurface = InkDark,
    surfaceVariant = SurfaceVariantDark, onSurfaceVariant = InkMutedDark,
    outline = LineDark, outlineVariant = LineDark,
    error = ExpenseRed, onError = Color.White,
)

@Composable
fun FinanceTheme(
    themeMode: ThemeMode,
    colorScheme: AppColorScheme,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val accent = when (colorScheme) {
        AppColorScheme.PURPLE -> purple
        AppColorScheme.ORANGE -> orange
    }
    val scheme = if (dark) darkScheme(accent) else lightScheme(accent)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                scheme.background.luminance() > 0.5f
        }
    }

    val accentColors = AccentColors(accent.primary, accent.gradientStart, accent.gradientEnd)
    CompositionLocalProvider(LocalAccentColors provides accentColors) {
        MaterialTheme(colorScheme = scheme, typography = AppTypography, shapes = AppShapes, content = content)
    }
}
