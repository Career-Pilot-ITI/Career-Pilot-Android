package com.iti.careerpilot.core.access

import com.iti.core.model.FeatureKey
import com.iti.core.model.Plan

internal object PlanAccessMap {
    private val map: Map<Plan, Set<FeatureKey>> = mapOf(
        Plan.FREE to setOf(
            FeatureKey.CvAiAnalysis,
            FeatureKey.MockInterviews
        ),
        Plan.PLUS to setOf(
            FeatureKey.CvAiAnalysis,
            FeatureKey.MockInterviews,
            FeatureKey.VoicePracticeMode,
            FeatureKey.ExportPdfReport
        ),
        Plan.MAX to setOf(
            FeatureKey.MockInterviews,
            FeatureKey.AdvancedReports,
            FeatureKey.CvAiAnalysis,
            FeatureKey.VoicePracticeMode,
            FeatureKey.ExportPdfReport
        )
    )

    fun featuresFor(plan: Plan): Set<FeatureKey> = map[plan].orEmpty()

    fun minimumPlanFor(feature: FeatureKey): Plan =
        Plan.entries.firstOrNull { feature in featuresFor(it) } ?: Plan.MAX
}
