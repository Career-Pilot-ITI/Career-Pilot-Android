package com.iti.careerpilot.bodylanguage.engine

import android.content.Context
import android.util.Log
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult

private const val TAG = "FaceLandmarkerEngine"
private const val MODEL_PATH = "models/face_landmarker.task"

internal class FaceLandmarkerEngine(
    private val context: Context,
    private val onResult: (FaceLandmarkerResult, Long) -> Unit,
) {
    private var landmarker: FaceLandmarker? = null

    fun initialize() {
        landmarker = try {
            createLandmarker(Delegate.GPU)
        } catch (e: Exception) {
            Log.w(TAG, "GPU delegate failed, falling back to CPU", e)
            createLandmarker(Delegate.CPU)
        }
    }

    private fun createLandmarker(delegate: Delegate): FaceLandmarker {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath(MODEL_PATH)
            .setDelegate(delegate)
            .build()

        val options = FaceLandmarker.FaceLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumFaces(1)
            .setMinFaceDetectionConfidence(0.5f)
            .setMinFacePresenceConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setOutputFaceBlendshapes(true)
            .setOutputFacialTransformationMatrixes(true)
            .setResultListener { result, input ->
                onResult(result, input.timestamp)
            }
            .setErrorListener { e ->
                Log.e(TAG, "Face detection error", e)
            }
            .build()

        return FaceLandmarker.createFromOptions(context, options)
    }

    fun detectAsync(image: MPImage, timestampMs: Long) {
        landmarker?.detectAsync(image, timestampMs)
    }

    fun close() {
        landmarker?.close()
        landmarker = null
    }
}
