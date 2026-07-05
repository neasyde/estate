package com.financeapp.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.financeapp.core.domain.model.TransactionWithCategory
import com.financeapp.core.ui.icons.materialIcon
import com.financeapp.core.ui.theme.ExpenseRed
import com.financeapp.core.ui.theme.IncomeGreen

/**
 * A transaction row with swipe actions — swipe left to delete, swipe right to duplicate,
 * tap to edit. Shared by the Transactions screen and the Home recent list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeTransactionRow(
    item: TransactionWithCategory,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onClick: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> { onDelete(); false }
                SwipeToDismissBoxValue.StartToEnd -> { onDuplicate(); false }
                else -> false
            }
        },
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> ExpenseRed
                SwipeToDismissBoxValue.StartToEnd -> IncomeGreen
                else -> Color.Transparent
            }
            val align = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Alignment.CenterEnd else Alignment.CenterStart
            val icon = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) "delete" else "content_copy"
            Box(
                Modifier.fillMaxSize().background(color).padding(horizontal = 24.dp),
                contentAlignment = align,
            ) {
                Icon(materialIcon(icon), contentDescription = null, tint = Color.White)
            }
        },
    ) {
        Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
            TransactionRow(item = item, onClick = onClick)
            Hairline(Modifier.padding(horizontal = 20.dp))
        }
    }
}
