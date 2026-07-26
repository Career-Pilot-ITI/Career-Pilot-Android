package com.iti.careerpilot.features.paywall.domain.usecase

import com.iti.careerpilot.features.paywall.domain.repository.PaymentRepository
import javax.inject.Inject

class TopUpWalletUseCase @Inject constructor(
    private val repository: PaymentRepository
) {
    suspend operator fun invoke(coinPackSize: Int, currency: String, method: String) = 
        repository.topUpWallet(coinPackSize, currency, method)
}
