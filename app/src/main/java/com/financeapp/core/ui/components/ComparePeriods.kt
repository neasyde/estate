package com.financeapp.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.financeapp.R
import java.text.NumberFormat

@Composable
fun ComparePeriods(
    thisMonthIncome: Double,
    thisMonthExpense: Double,
    lastMonthIncome: Double,
    lastMonthExpense: Double,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Eyebrow(stringResource(R.string.an_this_month))
                Text(
                    text = stringResource(R.string.an_income_label, NumberFormat.getNumberInstance().format(thisMonthIncome)),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Column(Modifier.weight(1f)) {
                Eyebrow(stringResource(R.string.an_last_month))
                Text(
                    text = stringResource(R.string.an_income_label, NumberFormat.getNumberInstance().format(lastMonthIncome)),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
