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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
            val listState = rememberLazyListState()
            var order by remember(cats) { mutableStateOf(cats) }
            var draggingId by remember { mutableStateOf<Long?>(null) }
            // Visual offset of the dragged row from its settled slot (follows the finger).
            var dragDelta by remember { mutableFloatStateOf(0f) }
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                itemsIndexed(order, key = { _, c -> c.id }) { i, c ->
                    val dragging = c.id == draggingId
                    // Dragged row: lift it and translate with the finger (no item animation).
                    // Every other row keeps animateItem() so it glides when displaced.
                    Column(
                        if (dragging) {
                            Modifier.zIndex(1f).graphicsLayer { translationY = dragDelta }
                        } else {
                            Modifier.animateItem()
                        },
                    ) {
                        CategoryManageRow(
                            category = c,
                            dragging = dragging,
                            handleModifier = Modifier.pointerInput(c.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { draggingId = c.id; dragDelta = 0f },
                                    onDragEnd = { vm.reorder(order.map { it.id }); draggingId = null; dragDelta = 0f },
                                    onDragCancel = { draggingId = null; dragDelta = 0f },
                                    onDrag = { change, amt ->
                                        change.consume()
                                        dragDelta += amt.y
                                        // Swap based on real laid-out positions: reorder only when
                                        // the dragged row's centre enters a neighbour's bounds. This
                                        // has natural hysteresis, so it can't jitter at boundaries.
                                        val info = listState.layoutInfo.visibleItemsInfo
                                        val self = info.firstOrNull { it.key == c.id }
                                        if (self != null) {
                                            val center = self.offset + dragDelta + self.size / 2f
                                            val over = info.firstOrNull { other ->
                                                other.key != c.id &&
                                                    center.toInt() in other.offset..(other.offset + other.size)
                                            }
                                            val toId = over?.key as? Long
                                            if (toId != null) {
                                                val from = order.indexOfFirst { it.id == c.id }
                                                val to = order.indexOfFirst { it.id == toId }
                                                if (from != -1 && to != -1 && from != to) {
                                                    order = order.toMutableList().also { it.add(to, it.removeAt(from)) }
                                                    // Keep the row under the finger: its settled slot
                                                    // moves from self.offset to the neighbour's offset.
                                                    dragDelta += (self.offset - over.offset).toFloat()
                                                }
                                            }
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
        // Extra padding widens the long-press-drag target so the handle is easy to grab.
        Icon(
            materialIcon("drag_handle"),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = handleModifier.padding(vertical = 8.dp, horizontal = 4.dp).size(24.dp),
        )
        Spacer(Modifier.width(8.dp))
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
