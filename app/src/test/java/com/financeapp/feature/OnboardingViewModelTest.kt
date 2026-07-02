package com.financeapp.feature

import com.financeapp.core.domain.model.AppLanguage
import com.financeapp.core.domain.usecase.SetPinUseCase
import com.financeapp.core.utils.PinHasher
import com.financeapp.feature.onboarding.OnboardingViewModel
import com.financeapp.testutil.FakeSettingsRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class OnboardingViewModelTest {
    @Test fun finishPersistsSettings() = runTest {
        val repo = FakeSettingsRepository()
        val vm = OnboardingViewModel(repo, SetPinUseCase(repo))
        vm.setLanguage(AppLanguage.EN)
        vm.setRateUsd("80")
        vm.setRateEur("95")
        vm.setPin("1234")
        vm.finish()

        val s = repo.current()
        assertThat(s.language).isEqualTo(AppLanguage.EN)
        assertThat(s.rateUsd).isEqualTo(80.0)
        assertThat(s.rateEur).isEqualTo(95.0)
        assertThat(s.pinHash).isEqualTo(PinHasher.hash("1234"))
        assertThat(s.onboardingCompleted).isTrue()
    }

    @Test fun finishWithoutPinLeavesPinNull() = runTest {
        val repo = FakeSettingsRepository()
        val vm = OnboardingViewModel(repo, SetPinUseCase(repo))
        vm.finish()
        assertThat(repo.current().pinHash).isNull()
        assertThat(repo.current().onboardingCompleted).isTrue()
    }
}
