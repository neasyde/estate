package com.financeapp.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Generous rounding across the board — modern fintech feel.
 * Drives Card (medium), TextField (extraSmall), Chip (small), BottomSheet (extraLarge), etc.
 */
// Quiet Minimal: restrained, gentle rounding (no big pill-cards).
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

/** Fully-rounded stadium shape for pills, chips and stat capsules. */
val PillShape = RoundedCornerShape(percent = 50)
