package com.financeapp.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun CalendarGrid(
    daysInMonth: Int,
    spendingByDay: Map<Int, Double>,
    maxSpending: Double,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        val weeks = (daysInMonth + 6) / 7
        for (week in 0 until weeks) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (dow in 0 until 7) {
                    val day = week * 7 + dow + 1
                    if (day <= daysInMonth) {
                        val spending = spendingByDay[day] ?: 0.0
                        val intensity = if (maxSpending > 0) (spending / maxSpending).coerceIn(0.0, 1.0) else 0.0
                        Box(
                            Modifier.weight(1f).aspectRatio(1f).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error.copy(alpha = (intensity * 0.7f).toFloat()))
                                .padding(2.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "$day",
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                            )
                        }
                    } else {
                        Box(Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}
