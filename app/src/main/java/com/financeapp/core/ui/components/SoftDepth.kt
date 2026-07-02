package com.financeapp.core.ui.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.financeapp.core.ui.anim.pressScale
import com.financeapp.core.ui.icons.materialIcon
import com.financeapp.core.ui.theme.EyebrowStyle
import com.financeapp.core.ui.theme.LocalAccentColors
import com.financeapp.core.ui.theme.PillShape

/**
 * Soft Depth signature: a coloured, blurred shadow so surfaces read as gently floating
 * above the page rather than boxed by borders. Colour tinting shows on API 28+.
 */
fun Modifier.softShadow(shape: Shape, color: Color, elevation: Dp = 12.dp): Modifier =
    this.shadow(elevation = elevation, shape = shape, clip = false, ambientColor = color, spotColor = color)

/** Diagonal accent gradient (top-start → bottom-end) used on the hero surface. */
@Composable
fun accentBrush(): Brush {
    val a = LocalAccentColors.current
    return Brush.linearGradient(listOf(a.gradientStart, a.gradientEnd))
}

/**
 * A rounded surface that floats on a soft accent-tinted shadow.
 * The default container is the theme surface; pass a brush-filled variant manually for the hero.
 */
@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    shadowColor: Color = LocalAccentColors.current.primary,
    elevation: Dp = 10.dp,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(vertical = 16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val interactive = if (onClick != null) {
        modifier
            .pressScale(interaction)
            .softShadow(shape, shadowColor, elevation)
            .clip(shape)
            .background(containerColor)
            .clickable(interactionSource = interaction, indication = LocalIndication.current, onClick = onClick)
    } else {
        modifier
            .softShadow(shape, shadowColor, elevation)
            .clip(shape)
            .background(containerColor)
    }
    Column(interactive.padding(contentPadding), content = content)
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
        Icon(materialIcon(icon), contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label.uppercase(), style = EyebrowStyle, color = contentColor.copy(alpha = 0.72f))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = contentColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
