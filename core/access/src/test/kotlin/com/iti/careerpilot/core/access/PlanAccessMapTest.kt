package com.iti.careerpilot.core.access

import com.iti.core.model.FeatureKey
import com.iti.core.model.Plan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanAccessMapTest {

    // ── FeatureKey constants ─────────────────────────────────────────────────

    @Test
    fun `FeatureKey VideoInterview has key VIDEO_INTERVIEW`() {
        assertEquals("VIDEO_INTERVIEW", FeatureKey.VideoInterview.key)
    }

    @Test
    fun `FeatureKey AtsFeatures has key ATS_FEATURES`() {
        assertEquals("ATS_FEATURES", FeatureKey.AtsFeatures.key)
    }

    @Test
    fun `FeatureKey Quizzes has key QUIZZES`() {
        assertEquals("QUIZZES", FeatureKey.Quizzes.key)
    }

    @Test
    fun `FeatureKey CoverLetter has key COVER_LETTER`() {
        assertEquals("COVER_LETTER", FeatureKey.CoverLetter.key)
    }

    @Test
    fun `FeatureKey JobParse has key JOB_PARSE`() {
        assertEquals("JOB_PARSE", FeatureKey.JobParse.key)
    }

    @Test
    fun `FeatureKey CreateChallenge has key CREATE_CHALLENGE`() {
        assertEquals("CREATE_CHALLENGE", FeatureKey.CreateChallenge.key)
    }

    @Test
    fun `FeatureKey EnterChallenge has key ENTER_CHALLENGE`() {
        assertEquals("ENTER_CHALLENGE", FeatureKey.EnterChallenge.key)
    }

    // ── displayName ──────────────────────────────────────────────────────────

    @Test
    fun `displayName for VideoInterview is non-empty`() {
        assertTrue(FeatureKey.VideoInterview.displayName().isNotBlank())
    }

    @Test
    fun `displayName for AtsFeatures is non-empty`() {
        assertTrue(FeatureKey.AtsFeatures.displayName().isNotBlank())
    }

    @Test
    fun `displayName for CoverLetter is non-empty`() {
        assertTrue(FeatureKey.CoverLetter.displayName().isNotBlank())
    }

    @Test
    fun `displayName for JobParse is non-empty`() {
        assertTrue(FeatureKey.JobParse.displayName().isNotBlank())
    }

    @Test
    fun `displayName for CreateChallenge matches expected label`() {
        assertEquals("Create custom interview challenges", FeatureKey.CreateChallenge.displayName())
    }

    @Test
    fun `displayName for EnterChallenge matches expected label`() {
        assertEquals("Participate in challenges", FeatureKey.EnterChallenge.displayName())
    }

    // ── PlanAccessMap spec ───────────────────────────────────────────────────

    @Test
    fun `FREE plan includes MockInterviews, VoicePracticeMode, and EnterChallenge`() {
        val freeFeatures = PlanAccessMap.featuresFor(Plan.FREE)
        assertTrue(FeatureKey.MockInterviews in freeFeatures)
        assertTrue(FeatureKey.VoicePracticeMode in freeFeatures)
        assertTrue(FeatureKey.EnterChallenge in freeFeatures)
        assertEquals(3, freeFeatures.size)
    }

    @Test
    fun `FREE plan does NOT include CV, ATS, Quiz, or MAX features or CreateChallenge`() {
        val freeFeatures = PlanAccessMap.featuresFor(Plan.FREE)
        assertFalse(FeatureKey.AtsFeatures in freeFeatures)
        assertFalse(FeatureKey.CvAiAnalysis in freeFeatures)
        assertFalse(FeatureKey.CoverLetter in freeFeatures)
        assertFalse(FeatureKey.JobParse in freeFeatures)
        assertFalse(FeatureKey.Quizzes in freeFeatures)
        assertFalse(FeatureKey.ExportPdfReport in freeFeatures)
        assertFalse(FeatureKey.AdvancedReports in freeFeatures)
        assertFalse(FeatureKey.VideoInterview in freeFeatures)
        assertFalse(FeatureKey.CreateChallenge in freeFeatures)
    }

    @Test
    fun `PLUS plan includes ATS, CV, CoverLetter, JobParse, Quizzes, ExportPdf, Mock, Voice, and EnterChallenge`() {
        val plusFeatures = PlanAccessMap.featuresFor(Plan.PLUS)
        assertTrue(FeatureKey.MockInterviews in plusFeatures)
        assertTrue(FeatureKey.VoicePracticeMode in plusFeatures)
        assertTrue(FeatureKey.AtsFeatures in plusFeatures)
        assertTrue(FeatureKey.CvAiAnalysis in plusFeatures)
        assertTrue(FeatureKey.CoverLetter in plusFeatures)
        assertTrue(FeatureKey.JobParse in plusFeatures)
        assertTrue(FeatureKey.Quizzes in plusFeatures)
        assertTrue(FeatureKey.ExportPdfReport in plusFeatures)
        assertTrue(FeatureKey.EnterChallenge in plusFeatures)
    }

    @Test
    fun `PLUS plan does NOT include MAX exclusive features or CreateChallenge`() {
        val plusFeatures = PlanAccessMap.featuresFor(Plan.PLUS)
        assertFalse(FeatureKey.VideoInterview in plusFeatures)
        assertFalse(FeatureKey.AdvancedReports in plusFeatures)
        assertFalse(FeatureKey.CreateChallenge in plusFeatures)
    }

    @Test
    fun `MAX plan includes all features including VideoInterview, AdvancedReports, EnterChallenge, and CreateChallenge`() {
        val maxFeatures = PlanAccessMap.featuresFor(Plan.MAX)
        assertTrue(FeatureKey.MockInterviews in maxFeatures)
        assertTrue(FeatureKey.VoicePracticeMode in maxFeatures)
        assertTrue(FeatureKey.AtsFeatures in maxFeatures)
        assertTrue(FeatureKey.CvAiAnalysis in maxFeatures)
        assertTrue(FeatureKey.CoverLetter in maxFeatures)
        assertTrue(FeatureKey.JobParse in maxFeatures)
        assertTrue(FeatureKey.Quizzes in maxFeatures)
        assertTrue(FeatureKey.ExportPdfReport in maxFeatures)
        assertTrue(FeatureKey.AdvancedReports in maxFeatures)
        assertTrue(FeatureKey.VideoInterview in maxFeatures)
        assertTrue(FeatureKey.EnterChallenge in maxFeatures)
        assertTrue(FeatureKey.CreateChallenge in maxFeatures)
    }

    // ── minimumPlanFor ───────────────────────────────────────────────────────

    @Test
    fun `minimumPlanFor FREE tier features is FREE`() {
        assertEquals(Plan.FREE, PlanAccessMap.minimumPlanFor(FeatureKey.MockInterviews))
        assertEquals(Plan.FREE, PlanAccessMap.minimumPlanFor(FeatureKey.VoicePracticeMode))
        assertEquals(Plan.FREE, PlanAccessMap.minimumPlanFor(FeatureKey.EnterChallenge))
    }

    @Test
    fun `minimumPlanFor PLUS tier features is PLUS`() {
        assertEquals(Plan.PLUS, PlanAccessMap.minimumPlanFor(FeatureKey.AtsFeatures))
        assertEquals(Plan.PLUS, PlanAccessMap.minimumPlanFor(FeatureKey.CvAiAnalysis))
        assertEquals(Plan.PLUS, PlanAccessMap.minimumPlanFor(FeatureKey.CoverLetter))
        assertEquals(Plan.PLUS, PlanAccessMap.minimumPlanFor(FeatureKey.JobParse))
        assertEquals(Plan.PLUS, PlanAccessMap.minimumPlanFor(FeatureKey.Quizzes))
        assertEquals(Plan.PLUS, PlanAccessMap.minimumPlanFor(FeatureKey.ExportPdfReport))
    }

    @Test
    fun `minimumPlanFor MAX tier features is MAX`() {
        assertEquals(Plan.MAX, PlanAccessMap.minimumPlanFor(FeatureKey.AdvancedReports))
        assertEquals(Plan.MAX, PlanAccessMap.minimumPlanFor(FeatureKey.VideoInterview))
        assertEquals(Plan.MAX, PlanAccessMap.minimumPlanFor(FeatureKey.CreateChallenge))
    }
}
