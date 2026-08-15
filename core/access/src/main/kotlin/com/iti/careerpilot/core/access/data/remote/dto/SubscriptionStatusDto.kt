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
    @SerialName("plan") val plan: String,
    @SerialName("features") val features: List<String> = emptyList(),
    @SerialName("quotas") val quotas: List<QuotaDto> = emptyList(),
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("coin_balance") val coinBalance: Int = 0
) {
    fun toDomain(syncTime: Instant = Clock.System.now()): AccessState {
        val parsedPlan = when (plan.uppercase().trim()) {
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

        val parsedExpiresAt = expiresAt?.let { runCatching { Instant.parse(it) }.getOrNull() }

        return AccessState(
            plan = parsedPlan,
            features = domainFeatures.ifEmpty { PlanAccessMap.featuresFor(parsedPlan) },
            quotas = domainQuotas,
            expiresAt = parsedExpiresAt,
            lastSyncedAt = syncTime,
            coinBalance = coinBalance
        )
    }
}
