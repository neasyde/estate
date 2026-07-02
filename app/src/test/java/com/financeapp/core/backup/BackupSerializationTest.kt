package com.financeapp.core.backup

import com.financeapp.core.data.local.entity.CategoryEntity
import com.financeapp.core.data.local.entity.ReminderEntity
import com.financeapp.core.data.local.entity.TransactionEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Test

class BackupSerializationTest {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @Test fun roundTripsAllData() {
        val original = BackupData(
            exportedAt = 123L,
            transactions = listOf(TransactionEntity(1, 100.0, "RUB", "EXPENSE", 2, "note", 999L, null)),
            categories = listOf(CategoryEntity(2, "cat_food", "restaurant", 42, "EXPENSE", true, false)),
            reminders = listOf(ReminderEntity(3, "rent", 5000.0, "RUB", 888L, 3, "MONTHLY")),
            settings = SettingsBackup(baseCurrency = "USD", rateUsd = 91.0, themeMode = "DARK"),
        )
        val text = json.encodeToString(BackupData.serializer(), original)
        val restored = json.decodeFromString(BackupData.serializer(), text)

        assertThat(restored).isEqualTo(original)
        assertThat(restored.transactions.first().amount).isEqualTo(100.0)
        assertThat(restored.categories.first().name).isEqualTo("cat_food")
        assertThat(restored.reminders.first().repeatType).isEqualTo("MONTHLY")
        assertThat(restored.settings.baseCurrency).isEqualTo("USD")
    }

    @Test fun toleratesMissingFieldsViaDefaults() {
        val minimal = """{"transactions":[],"settings":{"baseCurrency":"EUR"}}"""
        val data = json.decodeFromString(BackupData.serializer(), minimal)

        assertThat(data.categories).isEmpty()
        assertThat(data.settings.baseCurrency).isEqualTo("EUR")
        assertThat(data.settings.rateUsd).isEqualTo(90.0)
    }
}
