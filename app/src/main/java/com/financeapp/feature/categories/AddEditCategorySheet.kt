package com.financeapp.feature.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.financeapp.R
import com.financeapp.core.domain.model.Category
import com.financeapp.core.domain.model.CategoryType
import com.financeapp.core.ui.components.Eyebrow
import com.financeapp.core.ui.components.IconPicker
import com.financeapp.core.ui.theme.accentChipColors
import com.financeapp.core.utils.rememberHaptics
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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

    val localeContext = LocalContext.current
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.background) {
        CompositionLocalProvider(LocalContext provides localeContext) {
        Column(
            Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp).verticalScroll(rememberScrollState()),
        ) {
            Text(
                stringResource(if (form.id == null) R.string.cat_new else R.string.cat_edit),
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = form.name,
                onValueChange = vm::setName,
                label = { Text(stringResource(R.string.cat_field_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(20.dp))

            Eyebrow(stringResource(R.string.cat_field_icon))
            Spacer(Modifier.height(12.dp))
            IconPicker(
                selectedIcon = form.icon,
                onIconSelected = vm::setIcon,
            )
            Spacer(Modifier.height(20.dp))

            Eyebrow(stringResource(R.string.cat_field_type))
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TypeChip(stringResource(R.string.cat_type_expense), form.type == CategoryType.EXPENSE, Modifier.weight(1f)) { vm.setType(CategoryType.EXPENSE) }
                TypeChip(stringResource(R.string.cat_type_income), form.type == CategoryType.INCOME, Modifier.weight(1f)) { vm.setType(CategoryType.INCOME) }
                TypeChip(stringResource(R.string.cat_type_both), form.type == CategoryType.BOTH, Modifier.weight(1f)) { vm.setType(CategoryType.BOTH) }
            }
            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { scope.launch { if (vm.save()) { haptics(true); onDismiss() } else haptics(false) } },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            ) { Text(stringResource(R.string.action_save), style = MaterialTheme.typography.titleMedium) }
        }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) }, modifier = modifier, colors = accentChipColors())
}
