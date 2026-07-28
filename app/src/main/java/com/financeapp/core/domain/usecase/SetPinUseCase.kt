package com.financeapp.core.domain.usecase

import com.financeapp.core.domain.repository.SettingsRepository
import com.financeapp.core.utils.PinHasher
import javax.inject.Inject

class SetPinUseCase @Inject constructor(
    private val repo: SettingsRepository,
) {
    suspend operator fun invoke(pin: String) {
        if (pin.length != 4 || !pin.all { it.isDigit() }) {
            throw IllegalArgumentException("PIN must be exactly 4 digits")
        }
        repo.setPinHash(PinHasher.hash(pin))
    }
}
