package com.financeapp.feature.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financeapp.R
import com.financeapp.core.domain.model.Category
import com.financeapp.core.domain.model.CategoryType
import com.financeapp.core.ui.components.Eyebrow
import com.financeapp.core.ui.icons.iconMatches
import com.financeapp.core.ui.icons.materialIcon
import com.financeapp.core.ui.icons.pickableIcons
import com.financeapp.core.utils.rememberHaptics
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditCategorySheet(
    initial: Category?,
    defaultType: CategoryType,
    onDismiss: () -> Unit,
    vm: AddEditCategoryViewModel = hiltViewModel(),
) {
    val form by vm.form.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val haptics = rememberHaptics()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(initial) { vm.load(initial, defaultType) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp).verticalScroll(rememberScrollState()),
        ) {
            Text(
                stringResource(if (form.id == null) R.string.cat_new else R.string.cat_edit),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = form.name,
                onValueChange = vm::setName,
                label = { Text(stringResource(R.string.cat_field_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(16.dp))

            Eyebrow(stringResource(R.string.cat_field_icon))
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = form.iconQuery,
                onValueChange = vm::setIconQuery,
                label = { Text(stringResource(R.string.cat_search_icon)) },
                leadingIcon = { Icon(materialIcon("search"), null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
            val icons = pickableIcons.filter { iconMatches(it, form.iconQuery) }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                icons.forEach { name ->
                    val sel = form.icon == name
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { vm.setIcon(name) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            materialIcon(name),
                            contentDescription = name,
                            tint = if (sel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            Eyebrow(stringResource(R.string.cat_field_type))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TypeChip(stringResource(R.string.cat_type_expense), form.type == CategoryType.EXPENSE) { vm.setType(CategoryType.EXPENSE) }
                TypeChip(stringResource(R.string.cat_type_income), form.type == CategoryType.INCOME) { vm.setType(CategoryType.INCOME) }
                TypeChip(stringResource(R.string.cat_type_both), form.type == CategoryType.BOTH) { vm.setType(CategoryType.BOTH) }
            }
            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { scope.launch { if (vm.save()) { haptics(true); onDismiss() } else haptics(false) } },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.action_save)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}
