package com.iti.careerpilot.bodylanguage.signal

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.iti.careerpilot.bodylanguage.model.HandFrameSignal
import javax.inject.Inject
import kotlin.math.sqrt

/**
 * Extracts hand signals from HandLandmarker results.
 * Detects hand visibility, hand-to-face touch, and fidgeting.
 */
internal class HandSignalExtractor @Inject constructor() {

    companion object {
        private const val WRIST = 0
        private const val INDEX_TIP = 8
        // Touch threshold in normalized image coordinates
        private const val TOUCH_DISTANCE_THRESHOLD = 0.12f
    }

    private val previousWristPositions = mutableMapOf<String, Pair<Float, Float>>()

    /**
     * @param faceCenterNorm normalized (x, y) of face center from face landmarks.
     *   Null if no face detected this frame.
     */
    fun extract(
        result: HandLandmarkerResult,
        timestampMs: Long,
        faceCenterNorm: Pair<Float, Float>?,
    ): HandFrameSignal {
        val landmarks = result.landmarks()
        val handCount = landmarks.size

        if (handCount == 0) {
            previousWristPositions.clear()
            return HandFrameSignal(
                timestampMs = timestampMs,
                handsVisible = 0,
                handToFaceTouch = false,
                handMovementScore = null,
            )
        }

        var touchDetected = false

        if (faceCenterNorm != null) {
            for (handLandmarks in landmarks) {
                val wrist = handLandmarks[WRIST]
                val indexTip = handLandmarks[INDEX_TIP]

                val wristDist = normalizedDistance(wrist, faceCenterNorm)
                val tipDist = normalizedDistance(indexTip, faceCenterNorm)

                if (wristDist < TOUCH_DISTANCE_THRESHOLD || tipDist < TOUCH_DISTANCE_THRESHOLD) {
                    touchDetected = true
                    break
                }
            }
        }

        // Movement score: average wrist displacement tracked by handedness
        val handednesses = result.handedness()
        val currentWrists = mutableMapOf<String, Pair<Float, Float>>()
        val displacements = mutableListOf<Float>()

        for (i in 0 until handCount) {
            val handLandmarks = landmarks[i]
            val handednessLabel = handednesses.getOrNull(i)?.firstOrNull()?.categoryName()
                ?: if (i == 0) "Left" else "Right"
            val wrist = handLandmarks[WRIST]
            val currentPos = wrist.x() to wrist.y()
            currentWrists[handednessLabel] = currentPos

            previousWristPositions[handednessLabel]?.let { prevPos ->
                val dx = currentPos.first - prevPos.first
                val dy = currentPos.second - prevPos.second
                displacements.add(sqrt((dx * dx + dy * dy).toDouble()).toFloat())
            }
        }

        previousWristPositions.clear()
        previousWristPositions.putAll(currentWrists)

        val movementScore = if (displacements.isNotEmpty()) {
            val avgDisp = displacements.average().toFloat()
            // Normalize: normal gesturing ~0.02, fidgeting ~0.06+
            (avgDisp / 0.06f).coerceIn(0f, 1f)
        } else {
            0f
        }

        return HandFrameSignal(
            timestampMs = timestampMs,
            handsVisible = handCount,
            handToFaceTouch = touchDetected,
            handMovementScore = movementScore,
        )
    }

    private fun normalizedDistance(
        landmark: NormalizedLandmark,
        point: Pair<Float, Float>,
    ): Float {
        val dx = landmark.x() - point.first
        val dy = landmark.y() - point.second
        return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    fun reset() {
        previousWristPositions.clear()
    }
}
