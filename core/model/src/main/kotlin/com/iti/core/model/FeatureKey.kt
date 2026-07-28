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

        fun from(raw: String): FeatureKey = FeatureKey(raw.trim().uppercase())
    }
}
