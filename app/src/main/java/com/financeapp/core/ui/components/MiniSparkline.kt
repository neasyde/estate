package com.financeapp.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun MiniSparkline(
    values: List<Double>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
) {
    if (values.isEmpty()) return
    val max = values.max()
    val min = values.min()
    val range = (max - min).coerceAtLeast(0.001)

    modifier
        .fillMaxWidth()
        .height(32.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant)
        .padding(4.dp)
        .drawWithCache {
            val path = Path().also { p ->
                val w = size.width
                val h = size.height
                val step = if (values.size < 2) w else w / (values.size - 1)
                values.forEachIndexed { i, v ->
                    val x = i * step
                    val y = h - ((v - min) / range * h).toFloat()
                    if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
                }
            }
            val brush = lineColor
            onDrawBehind {
                drawPath(path, brush, style = Stroke(width = 2.dp.toPx()))
                val w = size.width
                val h = size.height
                val last = values.last()
                val lastY = h - ((last - min) / range * h).toFloat()
                drawCircle(brush, radius = 3.dp.toPx(), center = Offset(w, lastY))
            }
        }
}
