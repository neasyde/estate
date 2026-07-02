package com.financeapp.feature.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financeapp.R
import com.financeapp.core.domain.model.Category
import com.financeapp.core.domain.model.CategoryType
import com.financeapp.core.ui.categoryDisplayName
import com.financeapp.core.ui.components.CategoryIcon
import com.financeapp.core.ui.components.Eyebrow
import com.financeapp.core.ui.components.Hairline
import com.financeapp.core.ui.icons.materialIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(onBack: () -> Unit, vm: CategoriesViewModel = hiltViewModel()) {
    val tab by vm.tab.collectAsStateWithLifecycle()
    val cats by vm.categories.collectAsStateWithLifecycle()
    var sheetOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.cat_manage_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(materialIcon("arrow_back"), contentDescription = null) }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = null; sheetOpen = true }) {
                Icon(materialIcon("add"), contentDescription = null)
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = if (tab == CategoryType.EXPENSE) 0 else 1) {
                Tab(
                    selected = tab == CategoryType.EXPENSE,
                    onClick = { vm.setTab(CategoryType.EXPENSE) },
                    text = { Text(stringResource(R.string.cat_tab_expense)) },
                )
                Tab(
                    selected = tab == CategoryType.INCOME,
                    onClick = { vm.setTab(CategoryType.INCOME) },
                    text = { Text(stringResource(R.string.cat_tab_income)) },
                )
            }
            LazyColumn(Modifier.fillMaxSize()) {
                items(cats, key = { it.id }) { c ->
                    CategoryManageRow(
                        category = c,
                        onEdit = { if (c.isCustom) { editing = c; sheetOpen = true } },
                        onToggleHidden = { vm.toggleHidden(c) },
                        onDelete = { vm.delete(c.id) },
                    )
                    Hairline(Modifier.padding(horizontal = 20.dp))
                }
            }
        }
    }

    if (sheetOpen) {
        AddEditCategorySheet(initial = editing, defaultType = tab, onDismiss = { sheetOpen = false })
    }
}

@Composable
private fun CategoryManageRow(
    category: Category,
    onEdit: () -> Unit,
    onToggleHidden: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = category.isCustom, onClick = onEdit)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .alpha(if (category.isHidden) 0.45f else 1f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryIcon(category)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(categoryDisplayName(category), style = MaterialTheme.typography.titleMedium)
            if (!category.isCustom) Eyebrow(stringResource(R.string.cat_badge_system))
        }
        if (category.isCustom) {
            IconButton(onClick = onDelete) {
                Icon(materialIcon("delete"), contentDescription = stringResource(R.string.action_delete), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            IconButton(onClick = onToggleHidden) {
                Icon(
                    materialIcon(if (category.isHidden) "visibility_off" else "visibility"),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
