package com.iti.careerpilot.bodylanguage

import androidx.camera.core.ImageProxy
import com.iti.core.model.bodylanguage.BodyLanguageMetrics

/**
 * Public entry point for body language analysis.
 * Consumers call [start] to initialize analysis engines,
 * forward camera frames via [processImage], and call [finalizeSession]
 * when the session ends.
 *
 * All MediaPipe tasks and signal processing are managed internally.
 */
interface BodyLanguageAnalyzer {
    /**
     * Starts the analysis pipeline and initializes ML models in background.
     */
    fun start()

    /**
     * Processes an incoming camera frame for body language analysis.
     * Implementations are responsible for closing [imageProxy].
     */
    fun processImage(imageProxy: ImageProxy)

    /**
     * Controls whether frame signals are actively recorded for evaluation (e.g. only while candidate is answering).
     */
    fun setRecordingActive(active: Boolean)

    /**
     * Pauses recording telemetry at the given timestamp.
     */
    fun pauseRecording(timestampMs: Long)

    /**
     * Resumes recording telemetry at the given timestamp.
     */
    fun resumeRecording(timestampMs: Long)

    /**
     * Stops analysis and releases all GPU/native resources.
     */
    fun stop()

    /**
     * Returns aggregated metrics for the session. Call after [stop].
     */
    suspend fun finalizeSession(): BodyLanguageMetrics

    /**
     * Controls whether posture tracking is enabled.
     */
    fun enablePostureTracking(enabled: Boolean)

    /**
     * Controls whether hand tracking is enabled.
     */
    fun enableHandTracking(enabled: Boolean)

    /** Whether the analyzer is currently running. */
    val isRunning: Boolean

    /** Whether telemetry recording is currently active. */
    val isRecordingActive: Boolean
}
