package com.iti.careerpilot.features.paywall.data.remote

import android.util.Log
import com.iti.core.datastore.repo.UserProfileRepo
import com.iti.core.datastore.sync.UserProfileSync
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

@Singleton
class UserSyncManager @Inject constructor(
    private val paymentApi: PaymentRemoteDataSource,
    private val userProfileRepo: UserProfileRepo
) : UserProfileSync {
    override suspend fun syncWalletBalance() {
        try {
            val balanceResponse = paymentApi.getWalletBalance()
            userProfileRepo.updateUserProfile { profile ->
                profile.copy(
                    account = profile.account.copy(
                        coinBalance = balanceResponse.balance
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            runCatching { Log.w(TAG, "syncWalletBalance failed silently: ${e.message}") }
        }
    }

    override suspend fun syncSubscriptionTier() {
        try {
            val subscriptionResponse = paymentApi.getCurrentSubscription()
            val currentTier = subscriptionResponse.tier
            userProfileRepo.updateUserProfile { profile ->
                profile.copy(
                    account = profile.account.copy(
                        subscriptionTier = currentTier
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            runCatching { Log.w(TAG, "syncSubscriptionTier failed silently: ${e.message}") }
        }
    }

    suspend fun checkLatestTransactionStatus(merchantOrderId: String?): String? {
        return try {
            val history = paymentApi.getPaymentHistory()
            if (!merchantOrderId.isNullOrBlank()) {
                val match = history.content.find { it.merchantOrderId == merchantOrderId }
                if (match != null) return match.status
            }
            history.content.firstOrNull()?.status
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            runCatching { Log.w(TAG, "checkLatestTransactionStatus failed silently: ${e.message}") }
            null
        }
    }

    private companion object {
        const val TAG = "UserSyncManager"
    }
}
