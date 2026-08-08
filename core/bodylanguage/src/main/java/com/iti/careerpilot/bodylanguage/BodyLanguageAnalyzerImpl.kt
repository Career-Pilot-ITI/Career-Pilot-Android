package com.iti.careerpilot.bodylanguage

import android.content.Context
import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider

import androidx.lifecycle.LifecycleOwner
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.iti.careerpilot.bodylanguage.aggregation.KeyMomentDetector
import com.iti.careerpilot.bodylanguage.aggregation.SessionAggregator
import com.iti.careerpilot.bodylanguage.engine.FaceLandmarkerEngine
import com.iti.careerpilot.bodylanguage.engine.FrameScheduler
import com.iti.careerpilot.bodylanguage.engine.HandLandmarkerEngine
import com.iti.careerpilot.bodylanguage.engine.PoseLandmarkerEngine
import com.iti.careerpilot.bodylanguage.model.BodyLanguageMetrics
import com.iti.careerpilot.bodylanguage.signal.EyeContactApproximator
import com.iti.careerpilot.bodylanguage.signal.FaceSignalExtractor
import com.iti.careerpilot.bodylanguage.signal.HandSignalExtractor
import com.iti.careerpilot.bodylanguage.signal.PostureSignalExtractor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

private const val TAG = "BodyLanguageAnalyzer"

internal class BodyLanguageAnalyzerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : BodyLanguageAnalyzer {

    private val _isRunning = AtomicBoolean(false)
    override val isRunning: Boolean get() = _isRunning.get()

    // Engines — created on start(), closed on stop()
    private var faceEngine: FaceLandmarkerEngine? = null
    private var poseEngine: PoseLandmarkerEngine? = null
    private var handEngine: HandLandmarkerEngine? = null
    private var frameScheduler: FrameScheduler? = null

    // Signal extractors
    private val eyeContactApproximator = EyeContactApproximator()
    private val faceExtractor = FaceSignalExtractor(eyeContactApproximator)
    private val postureExtractor = PostureSignalExtractor()
    private val handExtractor = HandSignalExtractor()

    // Aggregation
    private val aggregator = SessionAggregator()
    private val keyMomentDetector = KeyMomentDetector()

    // Cross-task shared state: face center for hand-to-face distance
    @Volatile
    private var lastFaceCenterNorm: Pair<Float, Float>? = null

    private var cameraProvider: ProcessCameraProvider? = null

    @Suppress("DEPRECATION")
    override fun start(
        cameraProvider: ProcessCameraProvider,
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider?,
    ) {
        if (_isRunning.getAndSet(true)) return

        this.cameraProvider = cameraProvider
        aggregator.reset()
        keyMomentDetector.reset()
        postureExtractor.reset()
        handExtractor.reset()
        lastFaceCenterNorm = null

        // Initialize engines
        val face = FaceLandmarkerEngine(context) { result, ts ->
            val signal = faceExtractor.extract(result, ts)
            aggregator.addFace(signal)
            keyMomentDetector.onFaceFrame(signal)

            // Track face center for hand-to-face detection
            if (result.faceLandmarks().isNotEmpty()) {
                val nose = result.faceLandmarks().first()[1] // Nose tip
                lastFaceCenterNorm = nose.x() to nose.y()
            } else {
                lastFaceCenterNorm = null
            }
        }

        val pose = PoseLandmarkerEngine(context) { result, ts ->
            val signal = postureExtractor.extract(result, ts)
            aggregator.addPosture(signal)
            keyMomentDetector.onPostureFrame(signal)
        }

        val hand = HandLandmarkerEngine(context) { result, ts ->
            val signal = handExtractor.extract(result, ts, lastFaceCenterNorm)
            aggregator.addHand(signal)
            keyMomentDetector.onHandFrame(signal)
        }

        face.initialize()
        pose.initialize()
        hand.initialize()

        faceEngine = face
        poseEngine = pose
        handEngine = hand
        frameScheduler = FrameScheduler(face, pose, hand)

        // CameraX ImageAnalysis
        @Suppress("DEPRECATION")
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(480, 640))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()

        imageAnalysis.setAnalyzer(Dispatchers.Default.asExecutor()) { imageProxy ->
            val bitmap = imageProxy.toBitmap()
            val mpImage = BitmapImageBuilder(bitmap).build()
            val timestampMs = imageProxy.imageInfo.timestamp / 1_000 // Convert µs to ms

            frameScheduler?.onFrame(mpImage, timestampMs)
            imageProxy.close()
        }

        // Optional camera preview
        val preview = surfaceProvider?.let { provider ->
            Preview.Builder().build().also { previewUseCase ->
                previewUseCase.setSurfaceProvider(provider)
            }
        }

        cameraProvider.unbindAll()

        val useCases = listOfNotNull(imageAnalysis, preview).toTypedArray()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_FRONT_CAMERA,
            *useCases,
        )

        Log.d(TAG, "Body language analysis started")
    }

    override fun stop() {
        if (!_isRunning.getAndSet(false)) return

        cameraProvider?.unbindAll()
        cameraProvider = null

        faceEngine?.close()
        poseEngine?.close()
        handEngine?.close()

        faceEngine = null
        poseEngine = null
        handEngine = null
        frameScheduler = null

        Log.d(TAG, "Body language analysis stopped")
    }

    override suspend fun finalizeSession(): BodyLanguageMetrics =
        withContext(Dispatchers.Default) {
            val baseMetrics = aggregator.finalize()
            val keyMoments = keyMomentDetector.getKeyMoments()
            baseMetrics.copy(keyMoments = keyMoments)
        }
}
