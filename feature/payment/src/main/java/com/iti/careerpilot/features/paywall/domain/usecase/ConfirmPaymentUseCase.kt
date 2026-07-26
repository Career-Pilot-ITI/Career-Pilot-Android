package com.iti.careerpilot.features.paywall.domain.usecase

import com.iti.careerpilot.features.paywall.domain.repository.PaymentRepository
import javax.inject.Inject

class ConfirmPaymentUseCase @Inject constructor(
    private val repository: PaymentRepository
) {
    suspend operator fun invoke(merchantOrderId: String) = repository.confirmPayment(merchantOrderId)
}
