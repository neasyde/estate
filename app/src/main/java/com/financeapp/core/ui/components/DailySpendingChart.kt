package com.financeapp.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp

@Composable
fun DailySpendingChart(
    dailyAmounts: List<Pair<Int, Double>>,
    modifier: Modifier = Modifier,
) {
    if (dailyAmounts.isEmpty()) return
    val max = dailyAmounts.maxOfOrNull { it.second } ?: 1.0
    val barColor = MaterialTheme.colorScheme.error

    modifier
        .fillMaxWidth()
        .height(120.dp)
        .drawWithCache {
            val barWidth = size.width / dailyAmounts.size * 0.6f
            val gap = size.width / dailyAmounts.size * 0.4f
            onDrawBehind {
                dailyAmounts.forEachIndexed { i, (_, amount) ->
                    val barHeight = (amount / max).toFloat() * (size.height - 8f)
                    val x = i * (barWidth + gap)
                    drawRect(
                        color = barColor,
                        topLeft = Offset(x, size.height - barHeight),
                        size = Size(barWidth, barHeight),
                    )
                }
            }
        }
}
