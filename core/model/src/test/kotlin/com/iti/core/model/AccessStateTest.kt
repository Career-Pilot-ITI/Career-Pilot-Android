package com.iti.core.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessStateTest {
    @Test
    fun hasAccess_returnsTrue_whenFeatureInPlan() {
        val state = AccessState(
            plan = Plan.PLUS,
            features = setOf(FeatureKey.VoicePracticeMode),
            quotas = mapOf(FeatureKey.CvAiAnalysis to FeatureQuota(FeatureKey.CvAiAnalysis, remaining = 1, max = 5, coinCost = 20)),
            expiresAt = null,
            lastSyncedAt = null,
            coinBalance = 100
        )
        assertTrue(state.hasAccess(FeatureKey.VoicePracticeMode))
        assertFalse(state.hasAccess(FeatureKey.AdvancedReports))
    }
}
