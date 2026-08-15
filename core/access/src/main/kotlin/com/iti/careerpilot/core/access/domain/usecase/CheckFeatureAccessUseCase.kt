package com.iti.careerpilot.core.access.domain.usecase

import com.iti.careerpilot.core.access.FeaturePricingMap
import com.iti.careerpilot.core.access.PlanAccessMap
import com.iti.careerpilot.core.access.domain.AccessRepository
import com.iti.core.model.FeatureAccess
import com.iti.core.model.FeatureKey
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CheckFeatureAccessUseCase @Inject constructor(
    private val repository: AccessRepository
) {
    operator fun invoke(feature: FeatureKey): Flow<FeatureAccess> =
        repository.accessState.map { state ->
            val now = Clock.System.now()
            if (state.isCacheStale(now, maxAgeHours = 24)) {
                return@map FeatureAccess.StaleCacheBlocked
            }

            if (!state.hasAccess(feature)) {
                return@map FeatureAccess.Locked(PlanAccessMap.minimumPlanFor(feature))
            }

            val quota = state.quotas[feature]
            val remaining = quota?.remaining
            if (remaining != null && remaining > 0) {
                return@map FeatureAccess.Granted(quota = quota)
            }

            val cost = (quota?.coinCost?.takeIf { it > 0 })
                ?: FeaturePricingMap.coinCost(feature)

            if (cost > 0) {
                if (state.coinBalance < cost) {
                    FeatureAccess.CoinTopUpRequired(
                        feature = feature,
                        coinCost = cost,
                        currentCoins = state.coinBalance
                    )
                } else {
                    FeatureAccess.Granted(quota = quota)
                }
            } else if (remaining == 0) {
                FeatureAccess.Locked(PlanAccessMap.minimumPlanFor(feature))
            } else {
                FeatureAccess.Granted(quota = quota)
            }
        }
}
