package com.iti.careerpilot.bodylanguage

import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import com.iti.core.model.bodylanguage.BodyLanguageMetrics

/**
 * Public entry point for body language analysis.
 * Consumers call [start] with a camera provider and lifecycle owner,
 * then [finalizeSession] when the session ends.
 *
 * All MediaPipe tasks, CameraX ImageAnalysis, and signal processing
 * are managed internally.
 */
interface BodyLanguageAnalyzer {
    /**
     * Starts camera analysis. Binds ImageAnalysis + optional Preview use cases.
     * @param cameraProvider obtained from ProcessCameraProvider.getInstance()
     * @param lifecycleOwner for CameraX lifecycle binding
     * @param surfaceProvider optional surface provider for camera preview UI
     */
    fun start(
        cameraProvider: ProcessCameraProvider,
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider? = null,
    )

    /** Dynamically binds or unbinds the camera preview surface UI while analysis is running. */
    fun bindPreview(surfaceProvider: Preview.SurfaceProvider?)

    /** Stops analysis and releases all GPU/native resources. */
    fun stop()

    /** Returns aggregated metrics for the session. Call after [stop]. */
    suspend fun finalizeSession(): BodyLanguageMetrics

    /** Whether the analyzer is currently running. */
    val isRunning: Boolean
}
