package com.financeapp.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.financeapp.core.ui.anim.pressScale
import com.financeapp.core.ui.icons.MaterialIcon
import com.financeapp.core.ui.icons.symbol
import com.financeapp.core.ui.theme.LocalBrandType
import com.financeapp.core.ui.theme.PillShape

/**
 * Quiet Minimal container: flat (no shadow). Separation comes from whitespace, the surface
 * tint, or an optional hairline [border]. [elevation] is accepted but ignored (kept for
 * call-site compatibility during the redesign).
 */
@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    @Suppress("UNUSED_PARAMETER") elevation: Dp = 0.dp,
    border: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(vertical = 16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val shaped = Modifier
        .then(if (border) Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape) else Modifier)
        .clip(shape)
        .background(containerColor)
    val base = if (onClick != null) {
        modifier
            .pressScale(interaction)
            .then(shaped)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    } else {
        modifier.then(shaped)
    }
    Column(base.padding(contentPadding), content = content)
}

/** A stadium-shaped capsule: small icon + tracked label above a bold value. */
@Composable
fun StatPill(
    icon: String,
    label: String,
    value: String,
    contentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .clip(PillShape)
            .background(containerColor)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MaterialIcon(symbol(icon), contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label.uppercase(), style = LocalBrandType.current.eyebrow, color = contentColor.copy(alpha = 0.72f))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = contentColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
