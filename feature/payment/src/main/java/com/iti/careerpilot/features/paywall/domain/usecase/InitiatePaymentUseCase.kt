package com.iti.careerpilot.features.paywall.domain.usecase

import com.iti.careerpilot.features.paywall.domain.repository.PaymentRepository
import javax.inject.Inject

class InitiatePaymentUseCase @Inject constructor(
    private val repository: PaymentRepository
) {
    suspend operator fun invoke(amount: Double, currency: String, method: String, provider: String, purchaseType: String, coinPackSize: Int?, tier: String?) = 
        repository.initiatePayment(amount, currency, method, provider, purchaseType, coinPackSize, tier)
}
