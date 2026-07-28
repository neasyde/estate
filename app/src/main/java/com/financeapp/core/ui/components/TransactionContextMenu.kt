package com.financeapp.core.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.financeapp.core.ui.components.Hairline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.financeapp.R
import com.financeapp.core.domain.model.TransactionWithCategory
import kotlin.math.roundToInt

/**
 * Wraps a transaction row with a long-press context menu offering quick actions.
 * Long-press opens a small dropdown with edit / duplicate / delete.
 */
@Composable
fun TransactionWithContextMenu(
    item: TransactionWithCategory,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    content: @Composable () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    var pressOffset by remember { mutableStateOf(IntOffset.Zero) }
    val density = LocalDensity.current

    Box(
        Modifier
            .fillMaxWidth()
            .pointerInput(item.transaction.id) {
                detectTapGestures(
                    onLongPress = { offset ->
                        pressOffset = IntOffset(offset.x.roundToInt(), offset.y.roundToInt())
                        menuOpen = true
                    },
                )
            },
    ) {
        content()
        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false },
            offset = androidx.compose.ui.unit.DpOffset(
                x = with(density) { pressOffset.x.toDp() },
                y = with(density) { pressOffset.y.toDp() },
            ),
            properties = PopupProperties(focusable = true),
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_edit)) },
                onClick = { menuOpen = false; onEdit() },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_duplicate)) },
                onClick = { menuOpen = false; onDuplicate() },
            )
            Hairline()
            DropdownMenuItem(
                text = {
                    Text(
                        stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                },
                onClick = { menuOpen = false; onDelete() },
            )
        }
    }
}
