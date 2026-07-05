package com.financeapp.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
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
private val indigo = Accent(IndigoPrimary, IndigoSecondary, IndigoTertiary, IndigoPrimary, IndigoSecondary, IndigoContainerLight, IndigoContainerDark)
private val terracotta = Accent(TerracottaPrimary, TerracottaSecondary, TerracottaTertiary, TerracottaPrimary, TerracottaSecondary, TerracottaContainerLight, TerracottaContainerDark)

/** Accent colours exposed to composables that draw gradients / coloured depth (Soft Depth). */
data class AccentColors(val primary: Color, val gradientStart: Color, val gradientEnd: Color)

val LocalAccentColors = staticCompositionLocalOf {
    AccentColors(PurplePrimary, PurpleGradStart, PurpleGradEnd)
}

// Nudge a neutral toward the accent so background/surfaces/hairlines carry the theme's hue.
private fun tint(base: Color, accent: Color, fraction: Float) = lerp(base, accent, fraction)

// The secondary/tertiary containers, surface tint and inverse primary are set explicitly: left
// unset they fall back to Material's baseline purple, which leaked into FilterChips (type/currency
// chips), the time picker and snackbar actions. Tie them all to the accent so nothing reads violet.
private fun lightScheme(a: Accent) = lightColorScheme(
    primary = a.primary, onPrimary = Color.White,
    secondary = a.secondary, onSecondary = Color.White,
    tertiary = a.tertiary, onTertiary = Color.White,
    primaryContainer = a.containerLight, onPrimaryContainer = a.primary,
    secondaryContainer = a.containerLight, onSecondaryContainer = a.primary,
    tertiaryContainer = a.containerLight, onTertiaryContainer = a.primary,
    surfaceTint = a.primary, inversePrimary = a.tertiary,
    background = tint(PaperLight, a.primary, 0.04f), onBackground = InkLight,
    surface = tint(SurfaceLight, a.primary, 0.03f), onSurface = InkLight,
    surfaceVariant = tint(SurfaceVariantLight, a.primary, 0.11f), onSurfaceVariant = InkMutedLight,
    outline = tint(LineLight, a.primary, 0.11f), outlineVariant = tint(LineLight, a.primary, 0.11f),
    error = ExpenseRed, onError = Color.White,
)

private fun darkScheme(a: Accent) = darkColorScheme(
    primary = a.tertiary, onPrimary = Color(0xFF14120E),
    secondary = a.secondary, onSecondary = Color.White,
    tertiary = a.tertiary, onTertiary = Color.Black,
    primaryContainer = a.containerDark, onPrimaryContainer = Color.White,
    secondaryContainer = a.containerDark, onSecondaryContainer = Color.White,
    tertiaryContainer = a.containerDark, onTertiaryContainer = Color.White,
    surfaceTint = a.tertiary, inversePrimary = a.primary,
    background = tint(PaperDark, a.primary, 0.06f), onBackground = InkDark,
    surface = tint(SurfaceDark, a.primary, 0.07f), onSurface = InkDark,
    surfaceVariant = tint(SurfaceVariantDark, a.primary, 0.14f), onSurfaceVariant = InkMutedDark,
    outline = tint(LineDark, a.primary, 0.16f), outlineVariant = tint(LineDark, a.primary, 0.16f),
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
        AppColorScheme.INDIGO -> indigo
        AppColorScheme.TERRACOTTA -> terracotta
    }
    val scheme = if (dark) darkScheme(accent) else lightScheme(accent)

    val view = LocalView.current
    if (!view.isInEditMode) {
        val lightBars = scheme.background.luminance() > 0.5f
        val barColor = scheme.background.toArgb()
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            // Status bar and system navigation bar match the app background in both themes.
            window.statusBarColor = barColor
            window.navigationBarColor = barColor
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = lightBars
            controller.isAppearanceLightNavigationBars = lightBars
        }
    }

    val accentColors = AccentColors(accent.primary, accent.gradientStart, accent.gradientEnd)
    CompositionLocalProvider(LocalAccentColors provides accentColors) {
        MaterialTheme(colorScheme = scheme, typography = AppTypography, shapes = AppShapes, content = content)
    }
}
