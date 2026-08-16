package com.iti.core.model

sealed interface FeatureAccess {
    data object Unknown : FeatureAccess
    data class Granted(val quota: FeatureQuota? = null) : FeatureAccess
    data class Locked(val requiredPlan: Plan) : FeatureAccess
    data class CoinTopUpRequired(
        val feature: FeatureKey,
        val coinCost: Int,
        val currentCoins: Int
    ) : FeatureAccess
    data object StaleCacheBlocked : FeatureAccess
}
