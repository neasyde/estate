package com.financeapp.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.financeapp.core.ui.theme.ExpenseRed

@Composable
fun PinDots(filled: Int, total: Int = 4, error: Boolean, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        repeat(total) { i ->
            val on = i < filled
            val color = when {
                error -> ExpenseRed
                on -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outline
            }
            val dot = Modifier.size(18.dp).clip(CircleShape)
            Row(
                modifier = if (on || error) dot.background(color) else dot.border(2.dp, color, CircleShape),
            ) {}
        }
    }
}
