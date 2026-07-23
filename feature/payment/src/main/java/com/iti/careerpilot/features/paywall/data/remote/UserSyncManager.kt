package com.iti.careerpilot.features.paywall.data.remote

import android.util.Log
import com.iti.core.datastore.repo.UserProfileRepo
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

@Singleton
class UserSyncManager @Inject constructor(
    private val paymentApi: PaymentRemoteDataSource,
    private val userProfileRepo: UserProfileRepo
) {
    suspend fun syncWalletBalance() {
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

    suspend fun syncSubscriptionTier() {
        try {
            val subscriptionResponse = paymentApi.getCurrentSubscription()
            val effectiveTier = subscriptionResponse.pendingTier ?: subscriptionResponse.tier
            userProfileRepo.updateUserProfile { profile ->
                profile.copy(
                    account = profile.account.copy(
                        subscriptionTier = effectiveTier
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            runCatching { Log.w(TAG, "syncSubscriptionTier failed silently: ${e.message}") }
        }
    }

    private companion object {
        const val TAG = "UserSyncManager"
    }
}
