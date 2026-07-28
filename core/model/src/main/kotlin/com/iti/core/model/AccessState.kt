package com.iti.core.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class AccessState(
    val plan: Plan,
    val features: Set<FeatureKey>,
    val quotas: Map<FeatureKey, FeatureQuota>,
    val expiresAt: Instant?,
    val lastSyncedAt: Instant?,
    val coinBalance: Int
) {
    fun hasAccess(feature: FeatureKey): Boolean = feature in features

    fun isCacheStale(currentTime: Instant, maxAgeHours: Long = 24): Boolean {
        if (lastSyncedAt == null) return true
        val ageInSeconds = currentTime.epochSeconds - lastSyncedAt.epochSeconds
        return ageInSeconds > maxAgeHours * 3600
    }

    companion object {
        val Free = AccessState(Plan.FREE, emptySet(), emptyMap(), null, null, 0)
    }
}
