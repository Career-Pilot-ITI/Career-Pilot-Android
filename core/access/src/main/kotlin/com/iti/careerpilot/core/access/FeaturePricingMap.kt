package com.iti.careerpilot.core.access

import com.iti.core.model.FeatureKey

/**
 * Centralized feature coin pricing map matching backend values.
 *
 * Backend exact prices:
 * - Mock Interviews: 10 Coins (for standard 10m session)
 * - Voice Practice Mode: 10 Coins
 * - Video Interview: 0 Coins
 * - Quizzes: 0 Coins
 * - ATS Features: 5 Coins
 * - CV AI Analysis: 15 Coins
 * - Cover Letter: 5 Coins
 * - Job Parse: 1 Coin
 * - Export PDF Report: 0 Coins
 * - Advanced Reports: 0 Coins
 * - Create Challenge: 0 Coins
 * - Enter Challenge: 0 Coins
 */
object FeaturePricingMap {

    private val pricing: Map<FeatureKey, Int> = mapOf(
        FeatureKey.MockInterviews to 10,
        FeatureKey.VoicePracticeMode to 10,
        FeatureKey.VideoInterview to 0,
        FeatureKey.Quizzes to 0,
        FeatureKey.AtsFeatures to 5,
        FeatureKey.CvAiAnalysis to 15,
        FeatureKey.CoverLetter to 5,
        FeatureKey.JobParse to 1,
        FeatureKey.ExportPdfReport to 0,
        FeatureKey.AdvancedReports to 0,
        FeatureKey.CreateChallenge to 0,
        FeatureKey.EnterChallenge to 0,
    )

    /**
     * Returns the coin cost for a given [FeatureKey]. Defaults to 0 if not mapped.
     */
    fun coinCost(feature: FeatureKey): Int = pricing[feature] ?: 0

    /**
     * Returns an immutable view of all feature coin prices.
     */
    fun allPrices(): Map<FeatureKey, Int> = pricing
}
