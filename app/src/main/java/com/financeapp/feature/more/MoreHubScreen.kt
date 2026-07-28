package com.financeapp.feature.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.financeapp.R
import com.financeapp.core.ui.anim.pressScale
import com.financeapp.core.ui.components.Eyebrow
import com.financeapp.core.ui.icons.MaterialIcon
import com.financeapp.core.ui.icons.symbol

@Composable
fun MoreHubScreen(
    onNavigateToRecurring: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDonations: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.nav_more),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(20.dp))

        Eyebrow(stringResource(R.string.more_finance), color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        CardGroup {
            MoreCardRow("repeat", stringResource(R.string.recurring_title), onClick = onNavigateToRecurring)
        }

        Spacer(Modifier.height(16.dp))

        Eyebrow(stringResource(R.string.more_management), color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        CardGroup {
            MoreCardRow("edit", stringResource(R.string.cat_manage_title), onClick = onNavigateToCategories)
            MoreCardRow("notifications", stringResource(R.string.rem_title), onClick = onNavigateToReminders)
        }

        Spacer(Modifier.height(16.dp))

        Eyebrow(stringResource(R.string.more_app), color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        CardGroup {
            MoreCardRow("settings", stringResource(R.string.nav_settings), onClick = onNavigateToSettings)
        }

        Spacer(Modifier.height(16.dp))

        Eyebrow(stringResource(R.string.more_support), color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        CardGroup {
            MoreCardRow("favorite", stringResource(R.string.donations_title), onClick = onNavigateToDonations)
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun CardGroup(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.background,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 0.dp,
    ) {
        Column(Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun MoreCardRow(icon: String, label: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .pressScale(interaction)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            MaterialIcon(
                symbol(icon),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        MaterialIcon(
            symbol("chevron_right"),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
