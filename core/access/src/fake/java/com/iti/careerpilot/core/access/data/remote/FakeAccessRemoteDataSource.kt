package com.iti.careerpilot.core.access.data.remote

import com.iti.careerpilot.core.access.data.remote.dto.SubscriptionStatusDto
import com.iti.common.util.fakeDelay
import com.iti.core.datastore.repo.UserProfileRepo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAccessRemoteDataSource @Inject constructor(
    private val userProfileRepo: UserProfileRepo
) : AccessRemoteDataSource {

    override suspend fun getSubscriptionStatus(): SubscriptionStatusDto {
        fakeDelay()
        val profile = userProfileRepo.readUserProfile()
        val tier = profile.account.subscriptionTier.ifEmpty { "FREE" }
        val coins = if (profile.account.coinBalance > 0) profile.account.coinBalance else 250
        return SubscriptionStatusDto(
            tier = tier,
            isActive = true,
            startedAt = "2026-01-01T00:00:00Z",
            renewalDate = "2026-12-31T23:59:59Z",
            coinBalance = coins
        )
    }

    override suspend fun getWalletBalance(): Int {
        fakeDelay()
        val profile = userProfileRepo.readUserProfile()
        return if (profile.account.coinBalance > 0) profile.account.coinBalance else 250
    }
}
