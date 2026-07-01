package com.financeapp.core.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.financeapp.core.data.local.FinanceDatabase
import com.financeapp.core.data.local.entity.TransactionEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DaoTest {
    private lateinit var db: FinanceDatabase

    @Before fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, FinanceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After fun tearDown() = db.close()

    @Test fun insertAndObserveTransaction() = runTest {
        val id = db.transactionDao().upsert(
            TransactionEntity(
                amount = 50.0, currency = "RUB", type = "EXPENSE",
                categoryId = null, note = "test", date = 1000L, recurringRuleId = null,
            ),
        )
        assertThat(id).isGreaterThan(0L)
        val all = db.transactionDao().observeAll().first()
        assertThat(all).hasSize(1)
        assertThat(all.first().note).isEqualTo("test")
    }
}
