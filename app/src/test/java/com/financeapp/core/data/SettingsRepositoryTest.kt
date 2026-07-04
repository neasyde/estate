package com.financeapp.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import com.financeapp.core.data.repository.SettingsRepositoryImpl
import com.financeapp.core.domain.model.AutoLock
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

    @Test fun newSettingsDefaultsAndRoundTrip() = runTest {
        val r = repo()
        val def = r.settings.first()
        assertThat(def.showDecimals).isTrue()
        assertThat(def.autoBiometric).isTrue()
        assertThat(def.autoLock).isEqualTo(AutoLock.IMMEDIATE)
        r.setShowDecimals(false)
        r.setAutoLock(AutoLock.FIVE_MIN)
        r.setDefaultReminderHour(21)
        val s = r.settings.first()
        assertThat(s.showDecimals).isFalse()
        assertThat(s.autoLock).isEqualTo(AutoLock.FIVE_MIN)
        assertThat(s.defaultReminderHour).isEqualTo(21)
    }
}
