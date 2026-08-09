package com.iti.careerpilot.bodylanguage.aggregation

import com.iti.core.model.bodylanguage.BodyLanguageMetrics
import com.iti.careerpilot.bodylanguage.model.FaceFrameSignal
import com.iti.careerpilot.bodylanguage.model.HandFrameSignal
import com.iti.careerpilot.bodylanguage.model.PostureFrameSignal
import javax.inject.Inject

/**
 * Folds per-frame signals into running statistics. O(1) memory for sessions of any length.
 * Thread-safe: all mutations are synchronized.
 */
internal class SessionAggregator @Inject constructor() {

    // Face accumulators
    private var smileSum = 0.0
    private var smileCount = 0
    private var maxSmile = 0f
    private var eyeContactFrames = 0
    private var faceFrameCount = 0
    private var faceLostCount = 0
    private var lastFaceDetected = true
    private var lookAwayStartMs: Long? = null
    private var totalLookAwayMs = 0L

    // Posture accumulators
    private var torsoLeanSum = 0.0
    private var shoulderTiltSum = 0.0
    private var slouchFrames = 0
    private var postureFrameCount = 0
    private var postureChangeCount = 0
    private var lastSlouchAboveThreshold = false

    // Hand accumulators
    private var handsVisibleFrames = 0
    private var handFrameCount = 0
    private var handToFaceTouchCount = 0
    private var lastHandToFace = false
    private var fidgetScoreSum = 0.0
    private var fidgetCount = 0

    private var sessionStartMs: Long? = null
    private var sessionEndMs: Long? = null

    @Synchronized
    fun addFace(signal: FaceFrameSignal) {
        trackSessionTime(signal.timestampMs)
        faceFrameCount++

        if (!signal.faceDetected) {
            if (lastFaceDetected) faceLostCount++
            lastFaceDetected = false
            return
        }
        lastFaceDetected = true

        signal.smileScore?.let {
            smileSum += it
            smileCount++
            if (it > maxSmile) maxSmile = it
        }

        signal.lookingAtCamera?.let { looking ->
            if (looking) {
                eyeContactFrames++
                lookAwayStartMs?.let { start ->
                    totalLookAwayMs += signal.timestampMs - start
                    lookAwayStartMs = null
                }
            } else {
                if (lookAwayStartMs == null) {
                    lookAwayStartMs = signal.timestampMs
                }
            }
        }
    }

    @Synchronized
    fun addPosture(signal: PostureFrameSignal) {
        trackSessionTime(signal.timestampMs)
        if (!signal.poseDetected) return
        postureFrameCount++

        signal.torsoLeanDeg?.let { torsoLeanSum += it }
        signal.shoulderTiltDeg?.let { shoulderTiltSum += it }
        signal.slouchScore?.let { score ->
            val aboveThreshold = score > 0.4f
            if (aboveThreshold) slouchFrames++
            if (aboveThreshold != lastSlouchAboveThreshold) {
                postureChangeCount++
            }
            lastSlouchAboveThreshold = aboveThreshold
        }
    }

    @Synchronized
    fun addHand(signal: HandFrameSignal) {
        trackSessionTime(signal.timestampMs)
        handFrameCount++

        if (signal.handsVisible > 0) handsVisibleFrames++

        // Count distinct touch events (transition from not-touching to touching)
        if (signal.handToFaceTouch && !lastHandToFace) {
            handToFaceTouchCount++
        }
        lastHandToFace = signal.handToFaceTouch

        signal.handMovementScore?.let {
            fidgetScoreSum += it
            fidgetCount++
        }
    }

    @Synchronized
    fun finalize(): BodyLanguageMetrics {
        val durationMs = if (sessionStartMs != null && sessionEndMs != null) {
            sessionEndMs!! - sessionStartMs!!
        } else 0L

        // Close any open look-away span
        lookAwayStartMs?.let { start ->
            sessionEndMs?.let { end -> totalLookAwayMs += end - start }
        }

        return BodyLanguageMetrics(
            sessionDurationMs = durationMs,

            averageSmile = if (smileCount > 0) (smileSum / smileCount).toFloat() else 0f,
            maxSmile = maxSmile,
            eyeContactPercentage = if (faceFrameCount > 0) {
                (eyeContactFrames.toFloat() / faceFrameCount) * 100f
            } else 0f,
            timeLookingAwayMs = totalLookAwayMs,
            faceLostCount = faceLostCount,

            averageTorsoLeanDeg = if (postureFrameCount > 0) {
                (torsoLeanSum / postureFrameCount).toFloat()
            } else 0f,
            averageShoulderTiltDeg = if (postureFrameCount > 0) {
                (shoulderTiltSum / postureFrameCount).toFloat()
            } else 0f,
            slouchPercentage = if (postureFrameCount > 0) {
                (slouchFrames.toFloat() / postureFrameCount) * 100f
            } else 0f,
            postureChanges = postureChangeCount,

            handsVisiblePercentage = if (handFrameCount > 0) {
                (handsVisibleFrames.toFloat() / handFrameCount) * 100f
            } else 0f,
            handToFaceTouchCount = handToFaceTouchCount,
            fidgetScore = if (fidgetCount > 0) {
                (fidgetScoreSum / fidgetCount).toFloat()
            } else 0f,

            keyMoments = emptyList(), // Filled by KeyMomentDetector
        )
    }

    private fun trackSessionTime(timestampMs: Long) {
        if (sessionStartMs == null) sessionStartMs = timestampMs
        sessionEndMs = timestampMs
    }

    @Synchronized
    fun reset() {
        smileSum = 0.0; smileCount = 0; maxSmile = 0f
        eyeContactFrames = 0; faceFrameCount = 0; faceLostCount = 0
        lastFaceDetected = true; lookAwayStartMs = null; totalLookAwayMs = 0L
        torsoLeanSum = 0.0; shoulderTiltSum = 0.0; slouchFrames = 0
        postureFrameCount = 0; postureChangeCount = 0; lastSlouchAboveThreshold = false
        handsVisibleFrames = 0; handFrameCount = 0; handToFaceTouchCount = 0
        lastHandToFace = false; fidgetScoreSum = 0.0; fidgetCount = 0
        sessionStartMs = null; sessionEndMs = null
    }
}
