@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.financeapp.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.financeapp.R

private fun inter(weight: Int) = Font(
    resId = R.font.inter_variable,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

private fun fraunces(weight: Int, opsz: Float, soft: Float = 0f) = Font(
    resId = R.font.fraunces_variable,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight),
        FontVariation.Setting("opsz", opsz),
        FontVariation.Setting("SOFT", soft),
        FontVariation.Setting("WONK", 0f),
    ),
)

val InterFamily = FontFamily(inter(400), inter(500), inter(600), inter(700))

/** High optical size — high-contrast, for the balance masthead and large numbers. */
val FrauncesDisplay = FontFamily(fraunces(500, 128f), fraunces(600, 128f), fraunces(700, 128f))

/** Lower optical size — legible, for headings and section titles. */
val FrauncesTitle = FontFamily(fraunces(500, 36f), fraunces(600, 36f), fraunces(700, 36f))

/**
 * The three brand faces resolved for the current typeface choice, exposed to the handful of
 * composables that reach for a specific family directly (amount displays, editorial eyebrows,
 * Fraunces section titles). Switching to the system font swaps every one of them at the root so no
 * text is left on the branded serif/sans.
 */
data class BrandType(
    val display: FontFamily,
    val title: FontFamily,
    val sans: FontFamily,
    val eyebrow: TextStyle,
)

val brandedType = BrandType(FrauncesDisplay, FrauncesTitle, InterFamily, eyebrowStyle(InterFamily))
val systemType = BrandType(FontFamily.Default, FontFamily.Default, FontFamily.Default, eyebrowStyle(FontFamily.Default))

val LocalBrandType = staticCompositionLocalOf { brandedType }

/** Tracked-out uppercase eyebrow label style used across the editorial layout. */
private fun eyebrowStyle(family: FontFamily) = TextStyle(
    fontFamily = family,
    fontWeight = FontWeight.SemiBold,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = 1.6.sp,
)

/**
 * Material typography for the chosen typeface. Sizes/weights are identical between faces so the
 * layout is unchanged; only the family swaps. Sizes stay in [sp] so the app-controlled font scale
 * (see FinanceTheme) applies uniformly.
 */
fun appTypography(brand: BrandType): Typography = Typography(
    displayLarge = TextStyle(fontFamily = brand.display, fontWeight = FontWeight.SemiBold, fontSize = 46.sp, lineHeight = 50.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontFamily = brand.display, fontWeight = FontWeight.SemiBold, fontSize = 36.sp, lineHeight = 40.sp, letterSpacing = (-0.25).sp),
    displaySmall = TextStyle(fontFamily = brand.display, fontWeight = FontWeight.SemiBold, fontSize = 30.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontFamily = brand.title, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 34.sp),
    headlineSmall = TextStyle(fontFamily = brand.title, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = brand.title, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = brand.sans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = brand.sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = brand.sans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = brand.sans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = brand.sans, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = brand.sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp),
    labelMedium = TextStyle(fontFamily = brand.sans, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = brand.sans, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.5.sp),
)
