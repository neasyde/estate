package com.financeapp.core.ui.theme

import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
val IncomeGreen = PurplePrimary
val ExpenseRed = Color(0xFFB0492F)   // warm brick
val BudgetAmber = Color(0xFFE0A21B)
val IncomeGreenDark = PurpleTertiary
val ExpenseRedDark = Color(0xFFD2795E)

// Neutrals — warm off-white (light) / warm near-black (dark).
val PaperLight = Color(0xFFF5F0E8)
val SurfaceLight = Color(0xFFF8F3EB)
val SurfaceVariantLight = Color(0xFFEFEBE2)
val InkLight = Color(0xFF1A1712)
val InkMutedLight = Color(0xFF7E786B)
val LineLight = Color(0xFFEEEAE2)

val PaperDark = Color(0xFF15130E)
val SurfaceDark = Color(0xFF1B1813)
val SurfaceVariantDark = Color(0xFF221E17)
val InkDark = Color(0xFFEDEAE2)
val InkMutedDark = Color(0xFF8F8A7E)
val LineDark = Color(0xFF2A2620)

// Accent — muted indigo (calm, cool alt that still reads warm-neutral alongside the paper).
val IndigoPrimary = Color(0xFF41508F)
val IndigoSecondary = Color(0xFF5766A0)
val IndigoTertiary = Color(0xFF8C98D8)   // brighter — dark-theme accent

// Accent — terracotta (warm clay, distinct from amber & the brick expense colour).
val TerracottaPrimary = Color(0xFFBE5A43)
val TerracottaSecondary = Color(0xFFCE7059)
val TerracottaTertiary = Color(0xFFDD8E77)

// Subtle accent container tints per scheme.
// These are the *light* container colours used for selected chips, FAB, snackbar actions etc.
// They were originally very pale which made the FAB and "All/Income/Expense" chips nearly
// invisible against the warm-paper background. Push them deeper so the accent reads clearly.
val PurpleContainerLight = Color(0xFF9FC4B5)
val PurpleContainerDark = Color(0xFF1E3A31)
val OrangeContainerLight = Color(0xFFE5C189)
val OrangeContainerDark = Color(0xFF3A2C16)
val IndigoContainerLight = Color(0xFFAFB5DC)
val IndigoContainerDark = Color(0xFF232A47)
val TerracottaContainerLight = Color(0xFFE6B6A6)
val TerracottaContainerDark = Color(0xFF3A241D)

// 16-colour palette still stored for custom categories (icons render monochrome; kept for data).
val CategoryPalette = listOf(
    Color(0xFFEF5350), Color(0xFF42A5F5), Color(0xFF26A69A), Color(0xFFAB47BC),
    Color(0xFFEC407A), Color(0xFF5C6BC0), Color(0xFF8D6E63), Color(0xFF66BB6A),
    Color(0xFF29B6F6), Color(0xFF78909C), Color(0xFF43A047), Color(0xFF7E57C2),
    Color(0xFF00897B), Color(0xFFD81B60), Color(0xFFFFA726), Color(0xFF9CCC65),
)

@Composable
fun accentChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primary,
    selectedLabelColor = Color.White,
)
