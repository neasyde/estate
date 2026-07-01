package com.financeapp.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import com.financeapp.core.data.repository.SettingsRepositoryImpl
import com.financeapp.core.domain.model.ColorScheme
import com.financeapp.core.domain.model.Currency
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsRepositoryTest {
    private fun repo(): SettingsRepositoryImpl {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val ds: DataStore<Preferences> = PreferenceDataStoreFactory.create {
            ctx.preferencesDataStoreFile("test_settings_${System.nanoTime()}")
        }
        return SettingsRepositoryImpl(ds)
    }

    @Test fun defaultsThenPersist() = runTest {
        val r = repo()
        assertThat(r.settings.first().baseCurrency).isEqualTo(Currency.RUB)
        r.setColorScheme(ColorScheme.ORANGE)
        r.setPinHash("abc")
        val s = r.settings.first()
        assertThat(s.colorScheme).isEqualTo(ColorScheme.ORANGE)
        assertThat(s.pinHash).isEqualTo("abc")
    }
}
