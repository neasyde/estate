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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import com.financeapp.core.domain.model.AppFont
import com.financeapp.core.domain.model.AppFontSize
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
// Note: container colours are kept readable — `on*Container` is set to the *ink* colour (dark on
// light theme, light on dark) so text/legends on the small chip pills don't disappear.
private fun lightScheme(a: Accent) = lightColorScheme(
    primary = a.primary, onPrimary = Color.White,
    secondary = a.secondary, onSecondary = Color.White,
    tertiary = a.tertiary, onTertiary = Color.White,
    primaryContainer = a.containerLight, onPrimaryContainer = InkLight,
    secondaryContainer = a.containerLight, onSecondaryContainer = InkLight,
    tertiaryContainer = a.containerLight, onTertiaryContainer = InkLight,
    surfaceTint = a.primary, inversePrimary = a.tertiary,
    background = tint(PaperLight, a.primary, 0.04f), onBackground = InkLight,
    surface = tint(PaperLight, a.primary, 0.04f), onSurface = InkLight,
    surfaceVariant = tint(SurfaceVariantLight, a.primary, 0.11f), onSurfaceVariant = InkMutedLight,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = tint(PaperLight, a.primary, 0.02f),
    surfaceContainer = tint(PaperLight, a.primary, 0.03f),
    surfaceContainerHigh = tint(PaperLight, a.primary, 0.05f),
    surfaceContainerHighest = tint(PaperLight, a.primary, 0.07f),
    inverseSurface = InkLight, inverseOnSurface = PaperLight,
    scrim = Color.Black,
    outline = tint(LineLight, a.primary, 0.11f), outlineVariant = tint(LineLight, a.primary, 0.11f),
    error = ExpenseRed, onError = Color.White,
)

private val AmoledBlack = Color(0xFF000000)
private val AmoledSurface0A = Color(0xFF0A0A0A)
private val AmoledSurface05 = Color(0xFF050505)
private val AmoledSurface08 = Color(0xFF080808)
private val AmoledSurface0C = Color(0xFF0C0C0C)
private val AmoledSurface10 = Color(0xFF101010)
private val AmoledSurface14 = Color(0xFF141414)
private val AmoledOutline1A = Color(0xFF1A1A1A)

private fun darkScheme(a: Accent, amoled: Boolean = false) = darkColorScheme(
    primary = a.tertiary, onPrimary = Color(0xFF14120E),
    secondary = a.secondary, onSecondary = Color.White,
    tertiary = a.tertiary, onTertiary = Color.Black,
    primaryContainer = if (amoled) AmoledBlack else a.containerDark, onPrimaryContainer = Color.White,
    secondaryContainer = if (amoled) AmoledBlack else a.containerDark, onSecondaryContainer = Color.White,
    tertiaryContainer = if (amoled) AmoledBlack else a.containerDark, onTertiaryContainer = Color.White,
    surfaceTint = a.tertiary, inversePrimary = a.primary,
    background = if (amoled) AmoledBlack else tint(PaperDark, a.primary, 0.06f), onBackground = InkDark,
    surface = if (amoled) AmoledBlack else tint(PaperDark, a.primary, 0.06f), onSurface = InkDark,
    surfaceVariant = if (amoled) AmoledSurface0A else tint(SurfaceVariantDark, a.primary, 0.14f), onSurfaceVariant = InkMutedDark,
    surfaceContainerLowest = if (amoled) AmoledSurface05 else tint(PaperDark, a.primary, 0.02f),
    surfaceContainerLow = if (amoled) AmoledSurface08 else tint(PaperDark, a.primary, 0.04f),
    surfaceContainer = if (amoled) AmoledSurface0C else tint(PaperDark, a.primary, 0.05f),
    surfaceContainerHigh = if (amoled) AmoledSurface10 else tint(PaperDark, a.primary, 0.07f),
    surfaceContainerHighest = if (amoled) AmoledSurface14 else tint(PaperDark, a.primary, 0.09f),
    inverseSurface = InkDark, inverseOnSurface = PaperDark,
    scrim = Color.Black,
    outline = if (amoled) AmoledOutline1A else tint(LineDark, a.primary, 0.16f), outlineVariant = if (amoled) AmoledOutline1A else tint(LineDark, a.primary, 0.16f),
    error = ExpenseRed, onError = Color.White,
)

@Composable
fun FinanceTheme(
    themeMode: ThemeMode,
    colorScheme: AppColorScheme,
    appFont: AppFont = AppFont.BRANDED,
    fontSize: AppFontSize = AppFontSize.MEDIUM,
    amoledMode: Boolean = false,
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
    val scheme = if (dark) darkScheme(accent, amoledMode) else lightScheme(accent)

    val view = LocalView.current
    if (!view.isInEditMode) {
        val lightBars = scheme.background.luminance() > 0.5f
        val barColor = scheme.background.toArgb()
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            // Status bar and system navigation bar match the app background in both themes.
            @Suppress("DEPRECATION")
            window.statusBarColor = barColor
            @Suppress("DEPRECATION")
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
    val brand = if (appFont == AppFont.SYSTEM) systemType else brandedType

    // Drive text sizing from the app's own scale instead of the device font setting, so type renders
    // at the designed size on every phone regardless of the system accessibility font size, while
    // still letting the user pick a size in Settings.
    val baseDensity = LocalDensity.current
    val appDensity = Density(density = baseDensity.density, fontScale = fontSize.scale)

    CompositionLocalProvider(
        LocalAccentColors provides accentColors,
        LocalBrandType provides brand,
        LocalDensity provides appDensity,
    ) {
        MaterialTheme(colorScheme = scheme, typography = appTypography(brand), shapes = AppShapes, content = content)
    }
}
