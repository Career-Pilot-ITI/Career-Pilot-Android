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

    // ── displayName ──────────────────────────────────────────────────────────

    @Test
    fun `displayName for VideoInterview is non-empty`() {
        assertTrue(FeatureKey.VideoInterview.displayName().isNotBlank())
    }

    @Test
    fun `displayName for AtsFeatures is non-empty`() {
        assertTrue(FeatureKey.AtsFeatures.displayName().isNotBlank())
    }

    // ── PlanAccessMap spec ───────────────────────────────────────────────────

    @Test
    fun `FREE plan does NOT include AtsFeatures`() {
        assertFalse(FeatureKey.AtsFeatures in PlanAccessMap.featuresFor(Plan.FREE))
    }

    @Test
    fun `FREE plan does NOT include VideoInterview`() {
        assertFalse(FeatureKey.VideoInterview in PlanAccessMap.featuresFor(Plan.FREE))
    }

    @Test
    fun `FREE plan includes MockInterviews`() {
        assertTrue(FeatureKey.MockInterviews in PlanAccessMap.featuresFor(Plan.FREE))
    }

    @Test
    fun `PLUS plan includes AtsFeatures`() {
        assertTrue(FeatureKey.AtsFeatures in PlanAccessMap.featuresFor(Plan.PLUS))
    }

    @Test
    fun `PLUS plan includes Quizzes`() {
        assertTrue(FeatureKey.Quizzes in PlanAccessMap.featuresFor(Plan.PLUS))
    }

    @Test
    fun `PLUS plan does NOT include VideoInterview`() {
        assertFalse(FeatureKey.VideoInterview in PlanAccessMap.featuresFor(Plan.PLUS))
    }

    @Test
    fun `MAX plan includes VideoInterview`() {
        assertTrue(FeatureKey.VideoInterview in PlanAccessMap.featuresFor(Plan.MAX))
    }

    @Test
    fun `MAX plan includes AtsFeatures and Quizzes`() {
        assertTrue(FeatureKey.AtsFeatures in PlanAccessMap.featuresFor(Plan.MAX))
        assertTrue(FeatureKey.Quizzes in PlanAccessMap.featuresFor(Plan.MAX))
    }

    @Test
    fun `minimumPlanFor VideoInterview is MAX`() {
        assertEquals(Plan.MAX, PlanAccessMap.minimumPlanFor(FeatureKey.VideoInterview))
    }

    @Test
    fun `minimumPlanFor Quizzes is PLUS`() {
        assertEquals(Plan.PLUS, PlanAccessMap.minimumPlanFor(FeatureKey.Quizzes))
    }

    @Test
    fun `minimumPlanFor AtsFeatures is PLUS`() {
        assertEquals(Plan.PLUS, PlanAccessMap.minimumPlanFor(FeatureKey.AtsFeatures))
    }
}
