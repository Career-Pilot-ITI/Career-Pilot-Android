package com.iti.careerpilot.bodylanguage.engine

import android.content.Context
import android.util.Log
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

private const val TAG = "PoseLandmarkerEngine"
private const val MODEL_PATH = "models/pose_landmarker_lite.task"

internal open class PoseLandmarkerEngine(
    private val context: Context?,
    private val onResult: (PoseLandmarkerResult, Long) -> Unit,
) {
    private var landmarker: PoseLandmarker? = null

    fun initialize() {
        landmarker = try {
            createLandmarker(Delegate.GPU)
        } catch (e: Exception) {
            Log.w(TAG, "GPU delegate failed, falling back to CPU", e)
            createLandmarker(Delegate.CPU)
        }
    }

    private fun createLandmarker(delegate: Delegate): PoseLandmarker {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath(MODEL_PATH)
            .setDelegate(delegate)
            .build()

        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumPoses(1)
            .setMinPoseDetectionConfidence(0.5f)
            .setMinPosePresenceConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setResultListener { result, _ ->
                onResult(result, result.timestampMs())
            }
            .setErrorListener { e ->
                Log.e(TAG, "Pose detection error", e)
            }
            .build()

        return PoseLandmarker.createFromOptions(requireNotNull(context), options)
    }

    open fun detectAsync(image: MPImage, timestampMs: Long) {
        landmarker?.detectAsync(image, timestampMs)
    }

    open fun close() {
        landmarker?.close()
        landmarker = null
    }
}
