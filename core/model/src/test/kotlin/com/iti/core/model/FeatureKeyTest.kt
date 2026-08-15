package com.iti.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FeatureKeyTest {

    @Test
    fun `constants have expected key values`() {
        assertEquals("MOCK_INTERVIEW_SESSIONS", FeatureKey.MockInterviews.key)
        assertEquals("ADVANCED_REPORTS", FeatureKey.AdvancedReports.key)
        assertEquals("CV_AI_ANALYSIS", FeatureKey.CvAiAnalysis.key)
        assertEquals("VOICE_PRACTICE_MODE", FeatureKey.VoicePracticeMode.key)
        assertEquals("EXPORT_PDF_REPORT", FeatureKey.ExportPdfReport.key)
        assertEquals("VIDEO_INTERVIEW", FeatureKey.VideoInterview.key)
        assertEquals("ATS_FEATURES", FeatureKey.AtsFeatures.key)
        assertEquals("QUIZZES", FeatureKey.Quizzes.key)
        assertEquals("COVER_LETTER", FeatureKey.CoverLetter.key)
        assertEquals("JOB_PARSE", FeatureKey.JobParse.key)
    }

    @Test
    fun `displayName returns canonical strings`() {
        assertEquals("AI mock interview sessions", FeatureKey.MockInterviews.displayName())
        assertEquals("Advanced performance reports", FeatureKey.AdvancedReports.displayName())
        assertEquals("CV AI analysis", FeatureKey.CvAiAnalysis.displayName())
        assertEquals("Unlimited voice practice mode", FeatureKey.VoicePracticeMode.displayName())
        assertEquals("Export session reports as PDF", FeatureKey.ExportPdfReport.displayName())
        assertEquals("Video interview with body language analysis", FeatureKey.VideoInterview.displayName())
        assertEquals("ATS CV scoring & optimization", FeatureKey.AtsFeatures.displayName())
        assertEquals("Quiz-based interview practice", FeatureKey.Quizzes.displayName())
        assertEquals("AI cover letter generation", FeatureKey.CoverLetter.displayName())
        assertEquals("Job description parsing", FeatureKey.JobParse.displayName())
    }

    @Test
    fun `displayName fallback formats unknown feature key nicely`() {
        val unknown = FeatureKey("SOME_NEW_FEATURE")
        assertEquals("Some new feature", unknown.displayName())
    }
}
