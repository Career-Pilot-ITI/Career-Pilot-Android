package com.iti.careerpilot.core.access

import com.iti.core.model.FeatureKey
import com.iti.core.model.Plan

object PlanAccessMap {

    private val map: Map<Plan, Set<FeatureKey>> = mapOf(
        Plan.FREE to setOf(
            FeatureKey.MockInterviews,
            FeatureKey.VoicePracticeMode,
            FeatureKey.EnterChallenge,
        ),
        Plan.PLUS to setOf(
            FeatureKey.MockInterviews,
            FeatureKey.VoicePracticeMode,
            FeatureKey.AtsFeatures,
            FeatureKey.CvAiAnalysis,
            FeatureKey.CoverLetter,
            FeatureKey.JobParse,
            FeatureKey.Quizzes,
            FeatureKey.ExportPdfReport,
            FeatureKey.EnterChallenge,
        ),
        Plan.MAX to setOf(
            FeatureKey.MockInterviews,
            FeatureKey.VoicePracticeMode,
            FeatureKey.AtsFeatures,
            FeatureKey.CvAiAnalysis,
            FeatureKey.CoverLetter,
            FeatureKey.JobParse,
            FeatureKey.Quizzes,
            FeatureKey.ExportPdfReport,
            FeatureKey.AdvancedReports,
            FeatureKey.VideoInterview,
            FeatureKey.EnterChallenge,
            FeatureKey.CreateChallenge,
        ),
    )

    fun featuresFor(plan: Plan): Set<FeatureKey> = map[plan].orEmpty()

    fun minimumPlanFor(feature: FeatureKey): Plan =
        Plan.entries.firstOrNull { feature in featuresFor(it) } ?: Plan.MAX
}
