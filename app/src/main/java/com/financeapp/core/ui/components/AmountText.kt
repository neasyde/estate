package com.financeapp.core.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.ui.theme.ExpenseRed
import com.financeapp.core.ui.theme.FrauncesTitle
import com.financeapp.core.ui.theme.IncomeGreen
import com.financeapp.core.utils.CurrencyFormatter

@Composable
fun AmountText(
    amount: Double,
    currency: Currency,
    income: Boolean,
    style: TextStyle = LocalTextStyle.current,
) {
    val sign = if (income) "+" else "−"
    Text(
        text = sign + CurrencyFormatter.format(amount, currency),
        color = if (income) IncomeGreen else ExpenseRed,
        style = style.copy(fontFamily = FrauncesTitle),
        fontWeight = FontWeight.Normal,
    )
}
