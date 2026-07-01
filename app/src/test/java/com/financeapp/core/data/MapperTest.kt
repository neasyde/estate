package com.financeapp.core.data

import com.financeapp.core.data.mapper.toDomain
import com.financeapp.core.data.mapper.toEntity
import com.financeapp.core.domain.model.Category
import com.financeapp.core.domain.model.CategoryType
import com.financeapp.core.domain.model.Currency
import com.financeapp.core.domain.model.Transaction
import com.financeapp.core.domain.model.TransactionType
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MapperTest {
    @Test fun transactionRoundTrip() {
        val t = Transaction(
            id = 5, amount = 12.5, currency = Currency.USD, type = TransactionType.EXPENSE,
            categoryId = 3, note = "x", date = 100L, recurringRuleId = null,
        )
        assertThat(t.toEntity().toDomain()).isEqualTo(t)
    }

    @Test fun categoryRoundTrip() {
        val c = Category(
            id = 2, name = "cat_food", icon = "restaurant", color = -1,
            type = CategoryType.BOTH, isCustom = true,
        )
        assertThat(c.toEntity().toDomain()).isEqualTo(c)
    }
}
