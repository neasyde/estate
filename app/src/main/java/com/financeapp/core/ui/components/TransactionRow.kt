package com.financeapp.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.financeapp.core.ui.anim.pressScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.financeapp.R
import com.financeapp.core.domain.model.TransactionType
import com.financeapp.core.domain.model.TransactionWithCategory
import com.financeapp.core.ui.categoryDisplayName
import com.financeapp.core.utils.DateUtils

@Composable
fun TransactionRow(
    item: TransactionWithCategory,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val tx = item.transaction
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interaction)
            .clickable(interactionSource = interaction, indication = null) { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryIcon(item.category)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = item.category?.let { categoryDisplayName(it) } ?: stringResource(R.string.cat_other),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // Always show the time; append the note after it when present.
            val time = remember(tx.date) { DateUtils.timeLabel(tx.date) }
            val subtitle = if (tx.note.isNullOrBlank()) time else "$time · ${tx.note}"
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        AmountText(
            amount = tx.amount,
            currency = tx.currency,
            income = tx.type == TransactionType.INCOME,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
