package com.financeapp.feature

import com.financeapp.core.domain.model.AppSettings
import com.financeapp.core.domain.usecase.VerifyPinUseCase
import com.financeapp.core.utils.PinHasher
import com.financeapp.feature.lock.LockViewModel
import com.financeapp.testutil.FakeSettingsRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LockViewModelTest {
    private fun vm() = LockViewModel(
        VerifyPinUseCase(FakeSettingsRepository(AppSettings(pinHash = PinHasher.hash("1234")))),
    )

    @Test fun lockoutAfterFiveWrongAttempts() = runTest {
        val vm = vm()
        repeat(5) {
            "9999".forEach(vm::onDigit)
            vm.submit(now = 1000L)
        }
        assertThat(vm.state.value.attempts).isEqualTo(5)
        assertThat(vm.state.value.lockedUntil).isEqualTo(1000L + 30_000L)
        assertThat(vm.remainingLock(1000L)).isGreaterThan(0L)
    }

    @Test fun correctPinUnlocks() = runTest {
        val vm = vm()
        "1234".forEach(vm::onDigit)
        assertThat(vm.submit(1000L)).isTrue()
    }

    @Test fun wrongPinDoesNotUnlock() = runTest {
        val vm = vm()
        "0000".forEach(vm::onDigit)
        assertThat(vm.submit(1000L)).isFalse()
        assertThat(vm.state.value.error).isTrue()
    }
}
