package com.iti.careerpilot.core.access

import com.iti.core.model.FeatureKey
import com.iti.core.model.Plan

object PlanAccessMap {

    private val map: Map<Plan, Set<FeatureKey>> = mapOf(
        Plan.FREE to setOf(
            FeatureKey.MockInterviews,
        ),
        Plan.PLUS to setOf(
            FeatureKey.MockInterviews,
            FeatureKey.VoicePracticeMode,
            FeatureKey.AtsFeatures,
            FeatureKey.CvAiAnalysis,
            FeatureKey.Quizzes,
            FeatureKey.ExportPdfReport,
        ),
        Plan.MAX to setOf(
            FeatureKey.MockInterviews,
            FeatureKey.VoicePracticeMode,
            FeatureKey.AtsFeatures,
            FeatureKey.CvAiAnalysis,
            FeatureKey.Quizzes,
            FeatureKey.ExportPdfReport,
            FeatureKey.AdvancedReports,
            FeatureKey.VideoInterview,
        ),
    )

    fun featuresFor(plan: Plan): Set<FeatureKey> = map[plan].orEmpty()

    fun minimumPlanFor(feature: FeatureKey): Plan =
        Plan.entries.firstOrNull { feature in featuresFor(it) } ?: Plan.MAX
}
