package com.iti.careerpilot.core.access.data.remote.dto

import com.iti.careerpilot.core.access.PlanAccessMap
import com.iti.core.model.AccessState
import com.iti.core.model.FeatureKey
import com.iti.core.model.FeatureQuota
import com.iti.core.model.Plan
import kotlin.time.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuotaDto(
    @SerialName("feature") val feature: String,
    @SerialName("remaining") val remaining: Int? = null,
    @SerialName("max") val max: Int? = null,
    @SerialName("coin_cost") val coinCost: Int? = null
)

@Serializable
data class SubscriptionStatusDto(
    @SerialName("tier") val tier: String? = null,
    @SerialName("plan") val plan: String? = null,
    @SerialName("isActive") val isActive: Boolean? = null,
    @SerialName("startedAt") val startedAt: String? = null,
    @SerialName("renewalDate") val renewalDate: String? = null,
    @SerialName("cancelledAt") val cancelledAt: String? = null,
    @SerialName("pendingTier") val pendingTier: String? = null,
    @SerialName("features") val features: List<String> = emptyList(),
    @SerialName("quotas") val quotas: List<QuotaDto> = emptyList(),
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("coin_balance") val coinBalance: Int? = null,
    @SerialName("coinBalance") val camelCoinBalance: Int? = null
) {
    val effectivePlanString: String
        get() = tier ?: plan ?: "FREE"

    val effectiveCoinBalance: Int?
        get() = coinBalance ?: camelCoinBalance

    fun toDomain(
        syncTime: Instant = Clock.System.now(),
        coinBalanceFallback: Int = 0
    ): AccessState {
        val parsedPlan = when (effectivePlanString.uppercase().trim()) {
            "FREE" -> Plan.FREE
            "PLUS" -> Plan.PLUS
            "PRO", "MAX" -> Plan.MAX // backend sends "PRO"; app calls it "MAX"
            else -> Plan.FREE
        }
        val domainFeatures = features.map { FeatureKey.from(it) }.toSet()
        val domainQuotas = quotas.associate { q ->
            val key = FeatureKey.from(q.feature)
            key to FeatureQuota(key, q.remaining, q.max, q.coinCost)
        }

        val rawExpiry = expiresAt ?: renewalDate
        val parsedExpiresAt = rawExpiry?.let { runCatching { Instant.parse(it) }.getOrNull() }

        return AccessState(
            plan = parsedPlan,
            features = domainFeatures.ifEmpty { PlanAccessMap.featuresFor(parsedPlan) },
            quotas = domainQuotas,
            expiresAt = parsedExpiresAt,
            lastSyncedAt = syncTime,
            coinBalance = effectiveCoinBalance ?: coinBalanceFallback
        )
    }
}
