package com.financeapp.core.domain.usecase

import com.financeapp.core.domain.repository.SettingsRepository
import com.financeapp.core.utils.PinHasher
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class VerifyPinUseCase @Inject constructor(
    private val repo: SettingsRepository,
) {
    suspend operator fun invoke(pin: String): Boolean =
        repo.settings.first().pinHash == PinHasher.hash(pin)
}
