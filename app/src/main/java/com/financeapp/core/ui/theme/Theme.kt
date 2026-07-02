package com.financeapp.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.financeapp.core.domain.model.ColorScheme as AppColorScheme
import com.financeapp.core.domain.model.ThemeMode

private fun purpleLight() = lightColorScheme(
    primary = PurplePrimary, secondary = PurpleSecondary, tertiary = PurpleTertiary,
)
private fun purpleDark() = darkColorScheme(
    primary = PurplePrimary, secondary = PurpleSecondary, tertiary = PurpleTertiary,
)
private fun orangeLight() = lightColorScheme(
    primary = OrangePrimary, secondary = OrangeSecondary, tertiary = OrangeTertiary,
)
private fun orangeDark() = darkColorScheme(
    primary = OrangePrimary, secondary = OrangeSecondary, tertiary = OrangeTertiary,
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
    val scheme = when (colorScheme) {
        AppColorScheme.PURPLE -> if (dark) purpleDark() else purpleLight()
        AppColorScheme.ORANGE -> if (dark) orangeDark() else orangeLight()
    }
    MaterialTheme(colorScheme = scheme, typography = AppTypography, content = content)
}
