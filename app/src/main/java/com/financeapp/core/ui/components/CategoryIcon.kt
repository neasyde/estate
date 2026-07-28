package com.financeapp.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.financeapp.core.domain.model.Category
import com.financeapp.core.ui.icons.MaterialIcon

/** Monochrome: a neutral rounded tile with an ink-tinted rounded icon (no per-category colour). */
@Composable
fun CategoryIcon(category: Category?, size: Dp = 40.dp) {
    val iconSize = size * 0.6f
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.26f))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        MaterialIcon(
            name = category?.icon ?: "more_horiz",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(iconSize),
        )
    }
}
