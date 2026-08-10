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

    // Session time & active recording tracking
    private var sessionStartMs: Long? = null
    private var sessionEndMs: Long? = null
    private var isRecordingActive = true
    private var activeWindowStartMs: Long? = null
    private var lastActiveTimestampMs: Long? = null
    private var cumulativeActiveDurationMs = 0L

    @Synchronized
    fun pauseRecording(timestampMs: Long) {
        if (!isRecordingActive) return

        // Accumulate active duration for the current answering window
        activeWindowStartMs?.let { start ->
            cumulativeActiveDurationMs += (timestampMs - start).coerceAtLeast(0L)
            activeWindowStartMs = null
        }

        // Close any open look-away span up to pause time
        lookAwayStartMs?.let { start ->
            totalLookAwayMs += (timestampMs - start).coerceAtLeast(0L)
            lookAwayStartMs = null
        }

        // Reset edge-triggered states across pause boundary
        lastSlouchAboveThreshold = false
        lastHandToFace = false
        lastFaceDetected = true

        sessionEndMs = maxOf(sessionEndMs ?: timestampMs, timestampMs)
        isRecordingActive = false
    }

    @Synchronized
    fun resumeRecording(timestampMs: Long) {
        if (isRecordingActive) return

        isRecordingActive = true
        activeWindowStartMs = timestampMs
        lastActiveTimestampMs = timestampMs

        if (sessionStartMs == null) {
            sessionStartMs = timestampMs
        }
        sessionEndMs = maxOf(sessionEndMs ?: timestampMs, timestampMs)

        lookAwayStartMs = null
        lastSlouchAboveThreshold = false
        lastHandToFace = false
        lastFaceDetected = true
    }

    @Synchronized
    fun addFace(signal: FaceFrameSignal) {
        if (!isRecordingActive) return
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
        if (!isRecordingActive) return
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
        if (!isRecordingActive) return
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
        // Close any open look-away span
        lookAwayStartMs?.let { start ->
            val end = sessionEndMs ?: start
            totalLookAwayMs += (end - start).coerceAtLeast(0L)
            lookAwayStartMs = null
        }

        val currentActiveDelta = if (isRecordingActive && activeWindowStartMs != null) {
            val end = sessionEndMs ?: activeWindowStartMs!!
            (end - activeWindowStartMs!!).coerceAtLeast(0L)
        } else 0L
        val totalActiveDurationMs = cumulativeActiveDurationMs + currentActiveDelta

        return BodyLanguageMetrics(
            sessionDurationMs = totalActiveDurationMs,

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
        if (sessionStartMs == null) {
            sessionStartMs = timestampMs
        }
        sessionEndMs = maxOf(sessionEndMs ?: timestampMs, timestampMs)
        if (isRecordingActive) {
            if (activeWindowStartMs == null) {
                activeWindowStartMs = timestampMs
            }
            lastActiveTimestampMs = timestampMs
        }
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
        isRecordingActive = true
        activeWindowStartMs = null
        lastActiveTimestampMs = null
        cumulativeActiveDurationMs = 0L
    }
}
