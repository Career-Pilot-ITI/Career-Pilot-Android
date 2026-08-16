package com.iti.careerpilot.core.access

import com.iti.core.model.FeatureKey
import org.junit.Assert.assertEquals
import org.junit.Test

class FeaturePricingMapTest {

    @Test
    fun `coinCost returns exact backend prices for all known features`() {
        assertEquals(10, FeaturePricingMap.coinCost(FeatureKey.MockInterviews))
        assertEquals(10, FeaturePricingMap.coinCost(FeatureKey.VoicePracticeMode))
        assertEquals(0, FeaturePricingMap.coinCost(FeatureKey.VideoInterview))
        assertEquals(0, FeaturePricingMap.coinCost(FeatureKey.Quizzes))
        assertEquals(5, FeaturePricingMap.coinCost(FeatureKey.AtsFeatures))
        assertEquals(15, FeaturePricingMap.coinCost(FeatureKey.CvAiAnalysis))
        assertEquals(5, FeaturePricingMap.coinCost(FeatureKey.CoverLetter))
        assertEquals(1, FeaturePricingMap.coinCost(FeatureKey.JobParse))
        assertEquals(0, FeaturePricingMap.coinCost(FeatureKey.ExportPdfReport))
        assertEquals(0, FeaturePricingMap.coinCost(FeatureKey.AdvancedReports))
        assertEquals(0, FeaturePricingMap.coinCost(FeatureKey.CreateChallenge))
        assertEquals(0, FeaturePricingMap.coinCost(FeatureKey.EnterChallenge))
    }

    @Test
    fun `coinCost returns 0 for unknown features`() {
        val unknown = FeatureKey("UNKNOWN_CUSTOM_FEATURE")
        assertEquals(0, FeaturePricingMap.coinCost(unknown))
    }

    @Test
    fun `allPrices map contains correct mappings`() {
        val prices = FeaturePricingMap.allPrices()
        assertEquals(12, prices.size)
        assertEquals(10, prices[FeatureKey.MockInterviews])
        assertEquals(10, prices[FeatureKey.VoicePracticeMode])
        assertEquals(0, prices[FeatureKey.VideoInterview])
        assertEquals(0, prices[FeatureKey.Quizzes])
        assertEquals(5, prices[FeatureKey.AtsFeatures])
        assertEquals(15, prices[FeatureKey.CvAiAnalysis])
        assertEquals(5, prices[FeatureKey.CoverLetter])
        assertEquals(1, prices[FeatureKey.JobParse])
        assertEquals(0, prices[FeatureKey.ExportPdfReport])
        assertEquals(0, prices[FeatureKey.AdvancedReports])
        assertEquals(0, prices[FeatureKey.CreateChallenge])
        assertEquals(0, prices[FeatureKey.EnterChallenge])
    }
}
