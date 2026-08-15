package com.iti.core.model

import kotlinx.serialization.Serializable

@Serializable
data class FeatureKey(val key: String) {
    companion object {
        val MockInterviews = FeatureKey("MOCK_INTERVIEW_SESSIONS")
        val AdvancedReports = FeatureKey("ADVANCED_REPORTS")
        val CvAiAnalysis = FeatureKey("CV_AI_ANALYSIS")
        val VoicePracticeMode = FeatureKey("VOICE_PRACTICE_MODE")
        val ExportPdfReport = FeatureKey("EXPORT_PDF_REPORT")
        val VideoInterview = FeatureKey("VIDEO_INTERVIEW")
        val AtsFeatures = FeatureKey("ATS_FEATURES")
        val Quizzes = FeatureKey("QUIZZES")

        fun from(raw: String): FeatureKey = FeatureKey(raw.trim().uppercase())
    }

    /**
     * Human-readable label for this feature key.
     * Used in [FeatureGateBottomSheet] benefit lists — single source of truth.
     */
    fun displayName(): String = when (this) {
        MockInterviews -> "AI mock interview sessions"
        AdvancedReports -> "Advanced performance reports"
        CvAiAnalysis -> "CV AI analysis"
        VoicePracticeMode -> "Unlimited voice practice mode"
        ExportPdfReport -> "Export session reports as PDF"
        VideoInterview -> "Video interview with body language analysis"
        AtsFeatures -> "ATS CV scoring & optimization"
        Quizzes -> "Quiz-based interview practice"
        else -> key.replace('_', ' ').lowercase()
            .replaceFirstChar { it.uppercase() }
    }
}
