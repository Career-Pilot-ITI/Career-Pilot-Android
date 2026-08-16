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

            val hasPlanAccess = state.hasAccess(feature)
            val quota = state.quotas[feature]
            val remaining = quota?.remaining

            // If plan grants access AND quota remaining > 0 → granted
            if (hasPlanAccess && remaining != null && remaining > 0) {
                return@map FeatureAccess.Granted(quota = quota)
            }

            // Compute coin cost (from quota override or pricing map)
            val cost = (quota?.coinCost?.takeIf { it > 0 }) ?: FeaturePricingMap.coinCost(feature)

            return@map when {
                // Coin path available
                cost > 0 -> {
                    if (state.coinBalance >= cost) {
                        FeatureAccess.Granted(quota = quota) // afford with coins
                    } else {
                        FeatureAccess.CoinTopUpRequired(
                            feature = feature,
                            coinCost = cost,
                            currentCoins = state.coinBalance
                        )
                    }
                }
                // Quota exhausted with no coin fallback
                remaining == 0 -> FeatureAccess.Locked(PlanAccessMap.minimumPlanFor(feature))
                // No coin option + plan has feature → unlimited (quota null or no cost)
                hasPlanAccess -> FeatureAccess.Granted(quota = quota)
                // No plan access + no coin option → truly locked
                else -> FeatureAccess.Locked(PlanAccessMap.minimumPlanFor(feature))
            }
        }
}
