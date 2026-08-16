package com.iti.careerpilot.core.access.data

import com.iti.common.dispatcher.CareerPilotDispatchers
import com.iti.common.dispatcher.Dispatcher
import com.iti.common.dispatcher.di.ApplicationScope
import com.iti.careerpilot.core.access.data.local.AccessLocalDataSource
import com.iti.careerpilot.core.access.data.remote.AccessRemoteDataSource
import com.iti.careerpilot.core.access.domain.AccessRepository
import com.iti.core.datastore.repo.UserProfileRepo
import com.iti.core.model.AccessState
import com.iti.core.model.FeatureKey
import com.iti.core.model.Plan
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AccessRepositoryImpl @Inject constructor(
    private val remote: AccessRemoteDataSource,
    private val local: AccessLocalDataSource,
    private val userProfileRepo: UserProfileRepo,
    @Dispatcher(CareerPilotDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    @ApplicationScope private val scope: CoroutineScope
) : AccessRepository {

    override val accessState: StateFlow<AccessState> =
        local.accessStateFlow.stateIn(scope, SharingStarted.Eagerly, AccessState.Free)

    override suspend fun refresh(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val dto = remote.getSubscriptionStatus()
            val walletCoins = remote.getWalletBalance()
            val cachedProfile = userProfileRepo.userProfile.value
            val effectiveCoins = dto.effectiveCoinBalance ?: walletCoins.takeIf { it > 0 } ?: cachedProfile.account.coinBalance
            val domainState = dto.toDomain(coinBalanceFallback = effectiveCoins)
            local.save(domainState)
            userProfileRepo.updateUserProfile { profile ->
                profile.copy(
                    account = profile.account.copy(
                        subscriptionTier = domainState.plan.name,
                        coinBalance = domainState.coinBalance
                    )
                )
            }
        }.onFailure {
            val cachedProfile = userProfileRepo.userProfile.value
            if (cachedProfile.account.subscriptionTier.isNotEmpty() || cachedProfile.account.coinBalance > 0) {
                val cachedPlan = Plan.fromString(cachedProfile.account.subscriptionTier)
                val fallbackState = AccessState(
                    plan = cachedPlan,
                    features = com.iti.careerpilot.core.access.PlanAccessMap.featuresFor(cachedPlan),
                    quotas = emptyMap(),
                    expiresAt = null,
                    lastSyncedAt = kotlin.time.Clock.System.now(),
                    coinBalance = cachedProfile.account.coinBalance
                )
                local.save(fallbackState)
            }
        }
    }

    override fun hasAccess(feature: FeatureKey): Boolean =
        accessState.value.hasAccess(feature)

    override suspend fun clear() {
        local.clear()
        userProfileRepo.updateUserProfile { profile ->
            profile.copy(
                account = profile.account.copy(
                    subscriptionTier = "",
                    coinBalance = 0
                )
            )
        }
    }
}
