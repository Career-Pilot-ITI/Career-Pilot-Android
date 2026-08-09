package com.iti.careerpilot.bodylanguage.aggregation

import com.iti.careerpilot.bodylanguage.model.FaceFrameSignal
import com.iti.careerpilot.bodylanguage.model.HandFrameSignal
import com.iti.careerpilot.bodylanguage.model.PostureFrameSignal
import com.iti.core.model.bodylanguage.KeyMoment
import com.iti.core.model.bodylanguage.KeyMomentType
import javax.inject.Inject
import kotlin.math.abs

/**
 * Debounced key moment detection using hysteresis state machines.
 * A single noisy frame is NOT a key moment — requires sustained threshold crossing.
 */
internal class KeyMomentDetector @Inject constructor() {

    private val moments = mutableListOf<KeyMoment>()

    // Look-away state machine
    private var lookAwayStartMs: Long? = null
    private val lookAwayMinDurationMs = 1500L
    private val lookAwayYawThreshold = 20f

    // Slouch state machine
    private var slouchStartMs: Long? = null
    private val slouchMinDurationMs = 3000L
    private val slouchScoreThreshold = 0.5f

    // Hand-to-face state machine
    private var handToFaceStartMs: Long? = null
    private val handToFaceMinDurationMs = 800L

    // Face lost state machine
    private var faceLostStartMs: Long? = null
    private val faceLostMinDurationMs = 2000L

    @Synchronized
    fun onFaceFrame(signal: FaceFrameSignal) {
        // Look-away detection
        val isAway = signal.headYawDeg?.let { abs(it) > lookAwayYawThreshold } ?: false
        if (isAway && lookAwayStartMs == null) {
            lookAwayStartMs = signal.timestampMs
        }
        if (!isAway && lookAwayStartMs != null) {
            val duration = signal.timestampMs - lookAwayStartMs!!
            if (duration >= lookAwayMinDurationMs) {
                moments.add(
                    KeyMoment(
                        timestampMs = lookAwayStartMs!!,
                        type = KeyMomentType.EYE_CONTACT_LOST,
                        durationMs = duration,
                    )
                )
            }
            lookAwayStartMs = null
        }

        // Face lost detection
        if (!signal.faceDetected && faceLostStartMs == null) {
            faceLostStartMs = signal.timestampMs
        }
        if (signal.faceDetected && faceLostStartMs != null) {
            val duration = signal.timestampMs - faceLostStartMs!!
            if (duration >= faceLostMinDurationMs) {
                moments.add(
                    KeyMoment(
                        timestampMs = faceLostStartMs!!,
                        type = KeyMomentType.FACE_LOST,
                        durationMs = duration,
                    )
                )
            }
            faceLostStartMs = null
        }

        // Smile detection (point event, not duration-based)
        signal.smileScore?.let { score ->
            if (score > 0.7f) {
                // Only add if last smile moment was >5s ago
                val lastSmile = moments.lastOrNull { it.type == KeyMomentType.SMILE_PEAK }
                if (lastSmile == null || signal.timestampMs - lastSmile.timestampMs > 5000L) {
                    moments.add(
                        KeyMoment(
                            timestampMs = signal.timestampMs,
                            type = KeyMomentType.SMILE_PEAK,
                        )
                    )
                }
            }
        }
    }

    @Synchronized
    fun onPostureFrame(signal: PostureFrameSignal) {
        val isSlouching = signal.slouchScore?.let { it > slouchScoreThreshold } ?: false
        if (isSlouching && slouchStartMs == null) {
            slouchStartMs = signal.timestampMs
        }
        if (!isSlouching && slouchStartMs != null) {
            val duration = signal.timestampMs - slouchStartMs!!
            if (duration >= slouchMinDurationMs) {
                moments.add(
                    KeyMoment(
                        timestampMs = slouchStartMs!!,
                        type = KeyMomentType.SLOUCH_START,
                        durationMs = duration,
                    )
                )
            }
            slouchStartMs = null
        }
    }

    @Synchronized
    fun onHandFrame(signal: HandFrameSignal) {
        // Hand-to-face
        if (signal.handToFaceTouch && handToFaceStartMs == null) {
            handToFaceStartMs = signal.timestampMs
        }
        if (!signal.handToFaceTouch && handToFaceStartMs != null) {
            val duration = signal.timestampMs - handToFaceStartMs!!
            if (duration >= handToFaceMinDurationMs) {
                moments.add(
                    KeyMoment(
                        timestampMs = handToFaceStartMs!!,
                        type = KeyMomentType.HAND_FIDGET_SPIKE,
                        durationMs = duration,
                    )
                )
            }
            handToFaceStartMs = null
        }

        // Fidget detection
        signal.handMovementScore?.let { score ->
            if (score > 0.7f) {
                val lastFidget = moments.lastOrNull { it.type == KeyMomentType.HAND_FIDGET_SPIKE }
                if (lastFidget == null || signal.timestampMs - lastFidget.timestampMs > 3000L) {
                    moments.add(
                        KeyMoment(
                            timestampMs = signal.timestampMs,
                            type = KeyMomentType.HAND_FIDGET_SPIKE,
                        )
                    )
                }
            }
        }
    }

    @Synchronized
    fun getKeyMoments(): List<KeyMoment> = moments.toList()

    @Synchronized
    fun reset() {
        moments.clear()
        lookAwayStartMs = null
        slouchStartMs = null
        handToFaceStartMs = null
        faceLostStartMs = null
    }
}
