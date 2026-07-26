package com.iti.careerpilot.features.paywall.domain.usecase

import com.iti.careerpilot.features.paywall.domain.repository.PaymentRepository
import javax.inject.Inject

class GetCurrentSubscriptionUseCase @Inject constructor(
    private val repository: PaymentRepository
) {
    suspend operator fun invoke() = repository.getCurrentSubscription()
}
