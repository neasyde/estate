package com.financeapp.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.financeapp.core.ui.anim.reducedMotion
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun CelebrationOverlay(active: Boolean, modifier: Modifier = Modifier) {
    if (!active) return
    val reduced = reducedMotion()
    if (reduced) return

    var progress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) { progress = 1f }
    val animated by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1500),
        label = "confetti",
    )

    val confettiColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
    )

    val particles = remember(confettiColors) {
        List(60) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                color = confettiColors.random(),
                angle = Random.nextFloat() * 360f,
                speed = 0.5f + Random.nextFloat() * 1.5f,
                size = 4f + Random.nextFloat() * 6f,
            )
        }
    }

    Canvas(modifier.fillMaxSize()) {
        particles.forEach { p ->
            val rad = Math.toRadians(p.angle.toDouble())
            val dx = cos(rad).toFloat() * p.speed * animated * 300f
            val dy = sin(rad).toFloat() * p.speed * animated * 200f + animated * 600f
            val alpha = (1f - animated).coerceIn(0f, 1f)
            drawCircle(
                color = p.color.copy(alpha = alpha),
                radius = p.size,
                center = Offset(p.x * size.width + dx, p.y * size.height + dy),
            )
        }
    }
}

private data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val color: Color,
    val angle: Float,
    val speed: Float,
    val size: Float,
)
