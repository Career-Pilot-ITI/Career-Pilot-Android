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
        merchantOrderId: String? = null,
        targetTier: String? = null,
        expectedCoinDelta: Int = 0,
        maxAttempts: Int = 10,
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

            // 1. Balance or tier changed
            if (currentBalance != baselineBalance || currentTier != baselineTier) {
                return PollResult.Success
            }

            // 2. Target subscription tier matched
            if (!targetTier.isNullOrBlank() && currentTier.equals(targetTier, ignoreCase = true)) {
                return PollResult.Success
            }

            // 3. Expected coin delta matched
            if (expectedCoinDelta > 0 && currentBalance >= baselineBalance + expectedCoinDelta) {
                return PollResult.Success
            }

            // 4. Query backend transaction status by order ID
            val txStatus = userSyncManager.checkLatestTransactionStatus(merchantOrderId)
            if ("CONFIRMED".equals(txStatus, ignoreCase = true)) {
                runCatching {
                    userSyncManager.syncWalletBalance()
                    userSyncManager.syncSubscriptionTier()
                }
                return PollResult.Success
            } else if ("FAILED".equals(txStatus, ignoreCase = true)) {
                return PollResult.Failed(PaymentFailureReason.DECLINED)
            }

            delay(delayMs)
            attempts++
        }

        // Final status check after timeout
        val finalStatus = userSyncManager.checkLatestTransactionStatus(merchantOrderId)
        if ("CONFIRMED".equals(finalStatus, ignoreCase = true)) {
            runCatching {
                userSyncManager.syncWalletBalance()
                userSyncManager.syncSubscriptionTier()
            }
            return PollResult.Success
        }

        return PollResult.Failed(PaymentFailureReason.VERIFICATION_TIMEOUT)
    }
}
