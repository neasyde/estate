package com.financeapp.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Generous rounding across the board — modern fintech feel.
 * Drives Card (medium), TextField (extraSmall), Chip (small), BottomSheet (extraLarge), etc.
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(14.dp),
    small = RoundedCornerShape(18.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(30.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

/** Fully-rounded stadium shape for pills, chips and stat capsules. */
val PillShape = RoundedCornerShape(percent = 50)
