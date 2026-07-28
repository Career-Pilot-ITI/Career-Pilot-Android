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
            val domainState = dto.toDomain()
            local.save(domainState)
            userProfileRepo.updateUserProfile { profile ->
                profile.copy(
                    account = profile.account.copy(
                        subscriptionTier = domainState.plan.name,
                        coinBalance = domainState.coinBalance
                    )
                )
            }
        }
    }

    override suspend fun deductCoins(amount: Int) = withContext(ioDispatcher) {
        val current = accessState.value
        val updatedBalance = (current.coinBalance - amount).coerceAtLeast(0)
        val updatedState = current.copy(coinBalance = updatedBalance)
        local.save(updatedState)
        userProfileRepo.updateUserProfile { profile ->
            profile.copy(
                account = profile.account.copy(coinBalance = updatedBalance)
            )
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
