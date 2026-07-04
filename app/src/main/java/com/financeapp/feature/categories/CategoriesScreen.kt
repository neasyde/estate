package com.financeapp.feature.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
            var order by remember(cats) { mutableStateOf(cats) }
            var draggingId by remember { mutableStateOf<Long?>(null) }
            var dragOffsetY by remember { mutableStateOf(0f) }
            var rowHeightPx by remember { mutableStateOf(0f) }
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                itemsIndexed(order, key = { _, c -> c.id }) { i, c ->
                    val dragging = c.id == draggingId
                    CategoryManageRow(
                        category = c,
                        dragging = dragging,
                        modifier = Modifier
                            .onSizeChanged { if (rowHeightPx == 0f) rowHeightPx = it.height.toFloat() }
                            .zIndex(if (dragging) 1f else 0f)
                            .graphicsLayer { translationY = if (dragging) dragOffsetY else 0f },
                        handleModifier = Modifier.pointerInput(c.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { draggingId = c.id; dragOffsetY = 0f },
                                onDragEnd = { vm.reorder(order.map { it.id }); draggingId = null; dragOffsetY = 0f },
                                onDragCancel = { draggingId = null; dragOffsetY = 0f },
                                onDrag = { change, amt ->
                                    change.consume()
                                    dragOffsetY += amt.y
                                    val cur = order.indexOfFirst { it.id == draggingId }
                                    val h = if (rowHeightPx > 0f) rowHeightPx else 1f
                                    if (cur in 0 until order.lastIndex && dragOffsetY > h / 2) {
                                        order = order.toMutableList().also { it.add(cur + 1, it.removeAt(cur)) }
                                        dragOffsetY -= h
                                    } else if (cur > 0 && dragOffsetY < -h / 2) {
                                        order = order.toMutableList().also { it.add(cur - 1, it.removeAt(cur)) }
                                        dragOffsetY += h
                                    }
                                },
                            )
                        },
                        onEdit = { if (c.isCustom) { editing = c; sheetOpen = true } },
                        onToggleHidden = { vm.toggleHidden(c) },
                        onDelete = { vm.delete(c.id) },
                    )
                    if (i < order.lastIndex) Hairline(Modifier.padding(horizontal = 20.dp))
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
    dragging: Boolean,
    modifier: Modifier = Modifier,
    handleModifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onToggleHidden: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier
            .fillMaxWidth()
            .background(if (dragging) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
            .clickable(enabled = category.isCustom, onClick = onEdit)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .alpha(if (category.isHidden) 0.45f else 1f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            materialIcon("drag_handle"),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = handleModifier.size(24.dp),
        )
        Spacer(Modifier.width(12.dp))
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
