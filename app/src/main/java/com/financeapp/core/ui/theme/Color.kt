package com.financeapp.core.ui.theme

import androidx.compose.ui.graphics.Color

// Quiet Minimal (warm). Flat: no gradients/shadows; separation via whitespace + hairlines.
// NOTE: the "Purple*" names are now the GREEN accent (default) and "Orange*" the AMBER alt —
// names kept stable to avoid churn across the theme/settings wiring.

// Accent — deep green (default).
val PurplePrimary = Color(0xFF2F5D50)
val PurpleSecondary = Color(0xFF3E7A69)
val PurpleTertiary = Color(0xFF5FA98A)   // brighter — used as the dark-theme accent
val PurpleGradStart = Color(0xFF2F5D50)
val PurpleGradEnd = Color(0xFF3E7A69)

// Accent alt — warm amber/ochre.
val OrangePrimary = Color(0xFFB5701F)
val OrangeSecondary = Color(0xFFC98A3A)
val OrangeTertiary = Color(0xFFD69A4E)
val OrangeGradStart = Color(0xFFB5701F)
val OrangeGradEnd = Color(0xFFD69A4E)

// Semantic financial colours (warm, not neon).
val IncomeGreen = Color(0xFF2F6B4F)
val ExpenseRed = Color(0xFFB0492F)   // warm brick
val BudgetAmber = Color(0xFFE0A21B)
val IncomeGreenDark = Color(0xFF5FA98A)
val ExpenseRedDark = Color(0xFFD2795E)

// Neutrals — warm off-white (light) / warm near-black (dark).
val PaperLight = Color(0xFFF7F6F3)
val SurfaceLight = Color(0xFFFDFCFA)
val SurfaceVariantLight = Color(0xFFEFEBE2)
val InkLight = Color(0xFF1A1712)
val InkMutedLight = Color(0xFF9A9488)
val LineLight = Color(0xFFEEEAE2)

val PaperDark = Color(0xFF15130E)
val SurfaceDark = Color(0xFF1B1813)
val SurfaceVariantDark = Color(0xFF221E17)
val InkDark = Color(0xFFEDEAE2)
val InkMutedDark = Color(0xFF8F8A7E)
val LineDark = Color(0xFF2A2620)

// Subtle accent container tints per scheme.
val PurpleContainerLight = Color(0xFFE4EDE8)
val PurpleContainerDark = Color(0xFF1E3A31)
val OrangeContainerLight = Color(0xFFF3E8D8)
val OrangeContainerDark = Color(0xFF3A2C16)

// 16-colour palette still stored for custom categories (icons render monochrome; kept for data).
val CategoryPalette = listOf(
    Color(0xFFEF5350), Color(0xFF42A5F5), Color(0xFF26A69A), Color(0xFFAB47BC),
    Color(0xFFEC407A), Color(0xFF5C6BC0), Color(0xFF8D6E63), Color(0xFF66BB6A),
    Color(0xFF29B6F6), Color(0xFF78909C), Color(0xFF43A047), Color(0xFF7E57C2),
    Color(0xFF00897B), Color(0xFFD81B60), Color(0xFFFFA726), Color(0xFF9CCC65),
)
