package com.iti.careerpilot.core.access

import com.iti.core.model.FeatureKey
import com.iti.core.model.Plan
import org.junit.Assert.assertEquals
import org.junit.Test

class PlanAccessMapTest {
    @Test
    fun minimumPlanFor_returnsCorrectPlan() {
        assertEquals(Plan.FREE, PlanAccessMap.minimumPlanFor(FeatureKey.CvAiAnalysis))
        assertEquals(Plan.PLUS, PlanAccessMap.minimumPlanFor(FeatureKey.VoicePracticeMode))
        assertEquals(Plan.MAX, PlanAccessMap.minimumPlanFor(FeatureKey.AdvancedReports))
    }
}
