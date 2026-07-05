package com.financeapp.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.financeapp.core.domain.model.TransactionWithCategory
import com.financeapp.core.ui.icons.materialIcon
import com.financeapp.core.ui.theme.ExpenseRed
import com.financeapp.core.ui.theme.IncomeGreen
import kotlinx.coroutines.launch

/**
 * A transaction row with swipe actions — swipe left to delete, swipe right to duplicate,
 * tap to edit. Shared by the Transactions screen and the Home recent list.
 *
 * The row only travels far enough to uncover a single rounded action pill (~[RevealDp]) rather
 * than sliding the full width; releasing past the trigger fires the action and the row springs
 * back to centre, so the gesture stays repeatable and never flings a row off screen.
 */
private val RevealDp = 76.dp

@Composable
fun SwipeTransactionRow(
    item: TransactionWithCategory,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onClick: () -> Unit,
) {
    val revealPx = with(LocalDensity.current) { RevealDp.toPx() }
    val trigger = revealPx * 0.55f
    val offsetX = remember { Animatable(0f) }
    // Authoritative drag target so rapid deltas never read a stale animated value.
    var target by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
        val slid = offsetX.value
        if (slid != 0f) {
            val toDelete = slid < 0f
            Box(
                Modifier.matchParentSize()
                    .padding(horizontal = 12.dp, vertical = 5.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (toDelete) ExpenseRed else IncomeGreen)
                    .padding(horizontal = 24.dp),
                contentAlignment = if (toDelete) Alignment.CenterEnd else Alignment.CenterStart,
            ) {
                Icon(
                    materialIcon(if (toDelete) "delete" else "content_copy"),
                    contentDescription = null,
                    tint = Color.White,
                )
            }
        }
        Column(
            Modifier.fillMaxWidth()
                .graphicsLayer { translationX = offsetX.value }
                .background(MaterialTheme.colorScheme.background)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        target = (target + delta).coerceIn(-revealPx, revealPx)
                        scope.launch { offsetX.snapTo(target) }
                    },
                    onDragStopped = {
                        when {
                            offsetX.value <= -trigger -> onDelete()
                            offsetX.value >= trigger -> onDuplicate()
                        }
                        target = 0f
                        offsetX.animateTo(0f)
                    },
                ),
        ) {
            TransactionRow(item = item, onClick = onClick)
            Hairline(Modifier.padding(horizontal = 20.dp))
        }
    }
}
