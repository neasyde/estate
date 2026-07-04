package com.financeapp.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.financeapp.core.ui.anim.reducedMotion
import com.financeapp.core.ui.theme.ExpenseRed

@Composable
fun PinDots(filled: Int, total: Int = 4, error: Boolean, modifier: Modifier = Modifier) {
    val reduced = reducedMotion()
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        repeat(total) { i ->
            val on = i < filled
            val targetColor = when {
                error -> ExpenseRed
                on -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outline
            }
            val color by animateColorAsState(targetColor, tween(160), label = "pinColor")
            // Filled/error dots spring in with a little overshoot; empty dots sit slightly smaller.
            val scale by animateFloatAsState(
                targetValue = if (on || error) 1f else 0.72f,
                animationSpec = if (reduced) tween(0) else spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium),
                label = "pinScale",
            )
            Box(
                Modifier
                    .size(18.dp)
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                    .clip(CircleShape)
                    .then(if (on || error) Modifier.background(color) else Modifier.border(2.dp, color, CircleShape)),
            )
        }
    }
}
