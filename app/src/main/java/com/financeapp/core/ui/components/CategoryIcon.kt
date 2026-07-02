package com.financeapp.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.financeapp.core.domain.model.Category
import com.financeapp.core.ui.icons.materialIcon

@Composable
fun CategoryIcon(category: Category?, size: Dp = 40.dp) {
    val bg = category?.let { Color(it.color) } ?: MaterialTheme.colorScheme.surfaceVariant
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = materialIcon(category?.icon ?: "more_horiz"),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size * 0.55f),
        )
    }
}
