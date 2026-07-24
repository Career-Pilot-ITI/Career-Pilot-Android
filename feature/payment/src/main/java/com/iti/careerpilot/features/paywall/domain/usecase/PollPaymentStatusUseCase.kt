package com.iti.careerpilot.features.paywall.domain.usecase

import com.iti.careerpilot.features.paywall.data.remote.UserSyncManager
import com.iti.careerpilot.features.paywall.domain.model.PaymentFailureReason
import kotlinx.coroutines.delay
import javax.inject.Inject

sealed interface PollResult {
    data object Success : PollResult
    data class Failed(val reason: PaymentFailureReason) : PollResult
}

class PollPaymentStatusUseCase @Inject constructor(
    private val userSyncManager: UserSyncManager,
) {
    suspend operator fun invoke(
        baselineBalance: Int,
        baselineTier: String,
        getCurrentBalance: suspend () -> Int,
        getCurrentTier: suspend () -> String,
        maxAttempts: Int = 4,
        delayMs: Long = 1500L
    ): PollResult {
        var attempts = 0

        while (attempts < maxAttempts) {
            runCatching {
                userSyncManager.syncWalletBalance()
                userSyncManager.syncSubscriptionTier()
            }

            val currentBalance = getCurrentBalance()
            val currentTier = getCurrentTier()

            if (currentBalance != baselineBalance || currentTier != baselineTier) {
                return PollResult.Success
            }

            delay(delayMs)
            attempts++
        }

        return PollResult.Failed(PaymentFailureReason.VERIFICATION_TIMEOUT)
    }
}
