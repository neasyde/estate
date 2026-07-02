package com.financeapp.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.financeapp.core.ui.anim.Motion
import com.financeapp.core.ui.icons.materialIcon

/**
 * Empty-state placeholder. Lottie assets are deferred to a later phase; per the
 * spec's graceful-degradation rule this shows a static Material icon.
 */
@Composable
fun EmptyState(
    iconName: String,
    title: String,
    modifier: Modifier = Modifier,
    ctaText: String? = null,
    onCta: (() -> Unit)? = null,
) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val scale by animateFloatAsState(
        targetValue = if (shown) 1f else 0.92f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "emptyScale",
    )
    val appearAlpha by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(Motion.Medium),
        label = "emptyAlpha",
    )
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .alpha(appearAlpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = materialIcon(iconName),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(96.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (ctaText != null && onCta != null) {
            Spacer(Modifier.height(20.dp))
            Button(onClick = onCta) { Text(ctaText) }
        }
    }
}
