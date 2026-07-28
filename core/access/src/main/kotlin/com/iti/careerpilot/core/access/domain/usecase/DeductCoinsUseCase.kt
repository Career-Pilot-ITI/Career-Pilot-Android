package com.iti.careerpilot.core.access.domain.usecase

import com.iti.careerpilot.core.access.domain.AccessRepository
import javax.inject.Inject

class DeductCoinsUseCase @Inject constructor(
    private val repository: AccessRepository
) {
    suspend operator fun invoke(amount: Int) = repository.deductCoins(amount)
}
