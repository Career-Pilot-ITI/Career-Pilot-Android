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

internal class HandLandmarkerEngine(
    private val context: Context,
    private val onResult: (HandLandmarkerResult, Long) -> Unit,
) {
    private var landmarker: HandLandmarker? = null

    fun initialize() {
        landmarker = try {
            createLandmarker(Delegate.GPU)
        } catch (e: Exception) {
            Log.w(TAG, "GPU delegate failed, falling back to CPU", e)
            createLandmarker(Delegate.CPU)
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
            .setMinHandDetectionConfidence(0.5f)
            .setMinHandPresenceConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setResultListener { result, input ->
                onResult(result, input.timestamp)
            }
            .setErrorListener { e ->
                Log.e(TAG, "Hand detection error", e)
            }
            .build()

        return HandLandmarker.createFromOptions(context, options)
    }

    fun detectAsync(image: MPImage, timestampMs: Long) {
        landmarker?.detectAsync(image, timestampMs)
    }

    fun close() {
        landmarker?.close()
        landmarker = null
    }
}
