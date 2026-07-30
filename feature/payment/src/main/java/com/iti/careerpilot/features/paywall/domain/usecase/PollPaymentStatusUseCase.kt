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
        maxAttempts: Int = 3,
        delayMs: Long = 1500L
    ): PollResult {
        var attempts = 0

        while (attempts < maxAttempts) {
            // 1. Check transaction status by merchantOrderId first (single network request)
            val txStatus = userSyncManager.checkLatestTransactionStatus(merchantOrderId)
            if (isConfirmedStatus(txStatus)) {
                syncAll()
                return PollResult.Success
            } else if (isFailedStatus(txStatus)) {
                return PollResult.Failed(PaymentFailureReason.DECLINED)
            }

            // 2. Check balance or tier change if status is pending
            if (expectedCoinDelta > 0) {
                val currentBalance = runCatching { userSyncManager.syncWalletBalance() }.getOrElse { getCurrentBalance() }
                if (currentBalance != baselineBalance || currentBalance >= baselineBalance + expectedCoinDelta) {
                    return PollResult.Success
                }
            } else {
                val currentTier = runCatching { userSyncManager.syncSubscriptionTier() }.getOrElse { getCurrentTier() }
                if (currentTier != baselineTier || (!targetTier.isNullOrBlank() && currentTier.equals(targetTier, ignoreCase = true))) {
                    return PollResult.Success
                }
            }

            delay(delayMs)
            attempts++
        }

        // Final check after loop
        syncAll()
        val finalBalance = getCurrentBalance()
        val finalTier = getCurrentTier()
        if (finalBalance != baselineBalance || finalTier != baselineTier) {
            return PollResult.Success
        }

        val finalStatus = userSyncManager.checkLatestTransactionStatus(merchantOrderId)
        if (isConfirmedStatus(finalStatus)) {
            return PollResult.Success
        }

        return PollResult.Failed(PaymentFailureReason.VERIFICATION_TIMEOUT)
    }

    private suspend fun syncAll() {
        runCatching {
            userSyncManager.syncWalletBalance()
            userSyncManager.syncSubscriptionTier()
        }
    }

    private fun isConfirmedStatus(status: String?): Boolean {
        if (status.isNullOrBlank()) return false
        return status.equals("CONFIRMED", ignoreCase = true) ||
                status.equals("SUCCESS", ignoreCase = true) ||
                status.equals("PAID", ignoreCase = true)
    }

    private fun isFailedStatus(status: String?): Boolean {
        if (status.isNullOrBlank()) return false
        return status.equals("FAILED", ignoreCase = true) ||
                status.equals("DECLINED", ignoreCase = true) ||
                status.equals("CANCELLED", ignoreCase = true) ||
                status.equals("CANCELED", ignoreCase = true) ||
                status.equals("EXPIRED", ignoreCase = true) ||
                status.equals("REFUNDED", ignoreCase = true)
    }
}
