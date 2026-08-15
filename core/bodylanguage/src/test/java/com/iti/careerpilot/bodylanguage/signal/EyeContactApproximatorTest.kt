package com.iti.careerpilot.bodylanguage.signal

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EyeContactApproximatorTest {

    private val approximator = EyeContactApproximator()

    @Test
    fun isLookingAtCamera_nullPose_returnsNull() {
        val result = approximator.isLookingAtCamera(null, 5f, null)
        assertNull(result)
    }

    @Test
    fun isLookingAtCamera_yawExceedsThreshold_returnsFalse() {
        val result = approximator.isLookingAtCamera(20f, 0f, null)
        assertEquals(false, result)
    }

    @Test
    fun isLookingAtCamera_pitchExceedsThreshold_returnsFalse() {
        val result = approximator.isLookingAtCamera(0f, -15f, null)
        assertEquals(false, result)
    }

    @Test
    fun isLookingAtCamera_withinPoseThresholdNoIris_returnsTrue() {
        val result = approximator.isLookingAtCamera(5f, -5f, null)
        assertEquals(true, result)
    }

    @Test
    fun isLookingAtCamera_irisCentered_returnsTrue() {
        // Create 474 landmarks list with centered irises
        val landmarks = MutableList<NormalizedLandmark>(474) {
            NormalizedLandmark.create(0.5f, 0.5f, 0f)
        }

        // Left eye: inner 133, outer 33, iris 468
        landmarks[133] = NormalizedLandmark.create(0.45f, 0.5f, 0f)
        landmarks[33] = NormalizedLandmark.create(0.55f, 0.5f, 0f)
        landmarks[468] = NormalizedLandmark.create(0.50f, 0.5f, 0f)

        // Right eye: inner 362, outer 263, iris 473
        landmarks[362] = NormalizedLandmark.create(0.65f, 0.5f, 0f)
        landmarks[263] = NormalizedLandmark.create(0.75f, 0.5f, 0f)
        landmarks[473] = NormalizedLandmark.create(0.70f, 0.5f, 0f)

        val result = approximator.isLookingAtCamera(0f, 0f, landmarks)
        assertEquals(true, result)
    }

    @Test
    fun isLookingAtCamera_irisOffCenter_returnsFalse() {
        val landmarks = MutableList<NormalizedLandmark>(474) {
            NormalizedLandmark.create(0.5f, 0.5f, 0f)
        }

        // Left eye inner 0.45, outer 0.55 -> center 0.50, width 0.10
        // Iris at 0.53 -> offset 0.03 / 0.10 = 0.30 (> 0.15 threshold)
        landmarks[133] = NormalizedLandmark.create(0.45f, 0.5f, 0f)
        landmarks[33] = NormalizedLandmark.create(0.55f, 0.5f, 0f)
        landmarks[468] = NormalizedLandmark.create(0.53f, 0.5f, 0f)

        landmarks[362] = NormalizedLandmark.create(0.65f, 0.5f, 0f)
        landmarks[263] = NormalizedLandmark.create(0.75f, 0.5f, 0f)
        landmarks[473] = NormalizedLandmark.create(0.70f, 0.5f, 0f)

        val result = approximator.isLookingAtCamera(0f, 0f, landmarks)
        assertEquals(false, result)
    }
}
