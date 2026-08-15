package com.iti.careerpilot.bodylanguage.engine

import android.content.Context
import android.util.Log
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

private const val TAG = "HandLandmarkerEngine"
private const val MODEL_PATH = "models/hand_landmarker.task"

internal open class HandLandmarkerEngine(
    private val context: Context?,
    private val onResult: (HandLandmarkerResult, Long) -> Unit,
) {
    private var landmarker: HandLandmarker? = null

    fun initialize() {
        landmarker = safeCreateLandmarker(TAG) { delegate ->
            createLandmarker(delegate)
        }
    }

    private fun createLandmarker(delegate: Delegate): HandLandmarker {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath(MODEL_PATH)
            .setDelegate(delegate)
            .build()

        val options = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumHands(2)
            .setMinHandDetectionConfidence(0.35f)
            .setMinHandPresenceConfidence(0.35f)
            .setMinTrackingConfidence(0.35f)
            .setResultListener { result, _ ->
                onResult(result, result.timestampMs())
            }
            .setErrorListener { e ->
                Log.e(TAG, "Hand detection error", e)
            }
            .build()

        return HandLandmarker.createFromOptions(requireNotNull(context), options)
    }

    open fun detectAsync(image: MPImage, timestampMs: Long) {
        landmarker?.detectAsync(image, timestampMs)
    }

    open fun close() {
        landmarker?.close()
        landmarker = null
    }
}
