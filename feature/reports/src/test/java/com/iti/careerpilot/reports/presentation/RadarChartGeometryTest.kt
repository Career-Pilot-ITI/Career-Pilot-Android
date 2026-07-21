package com.iti.careerpilot.reports.presentation

import androidx.compose.ui.geometry.Offset
import com.iti.careerpilot.reports.presentation.screen.details.view.components.normalizeRadarScore
import com.iti.careerpilot.reports.presentation.screen.details.view.components.radarVertices
import org.junit.Assert.assertEquals
import org.junit.Test

class RadarChartGeometryTest {
    @Test
    fun `normalization clamps values to chart range`() {
        assertEquals(0f, normalizeRadarScore(-20f), 0f)
        assertEquals(0.5f, normalizeRadarScore(50f), 0f)
        assertEquals(1f, normalizeRadarScore(120f), 0f)
    }

    @Test
    fun `five values generate five vertices starting at top`() {
        val vertices = radarVertices(
            scores = FloatArray(5) { 1f },
            center = Offset(50f, 50f),
            radius = 50f,
        )

        assertEquals(5, vertices.size)
        assertEquals(50f, vertices.first().x, 0.001f)
        assertEquals(0f, vertices.first().y, 0.001f)
    }

    @Test
    fun `zero animation progress collapses data polygon to chart center`() {
        val center = Offset(50f, 50f)

        val vertices = radarVertices(
            scores = FloatArray(5) { 1f },
            center = center,
            radius = 50f,
            progress = 0f,
        )

        vertices.forEach { vertex ->
            assertEquals(center.x, vertex.x, 0f)
            assertEquals(center.y, vertex.y, 0f)
        }
    }
}
