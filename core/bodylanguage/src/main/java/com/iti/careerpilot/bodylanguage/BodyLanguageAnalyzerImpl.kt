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
import com.iti.core.model.bodylanguage.BodyLanguageMetrics
import com.iti.careerpilot.bodylanguage.signal.EyeContactApproximator
import com.iti.careerpilot.bodylanguage.signal.FaceSignalExtractor
import com.iti.careerpilot.bodylanguage.signal.HandSignalExtractor
import com.iti.careerpilot.bodylanguage.signal.PostureSignalExtractor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

private const val TAG = "BodyLanguageAnalyzer"

internal class BodyLanguageAnalyzerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : BodyLanguageAnalyzer {

    private val _isRunning = AtomicBoolean(false)
    override val isRunning: Boolean get() = _isRunning.get()

    private val _isRecordingActive = AtomicBoolean(true)
    override val isRecordingActive: Boolean get() = _isRecordingActive.get()

    override fun setRecordingActive(active: Boolean) {
        val wasActive = _isRecordingActive.getAndSet(active)
        if (wasActive != active) {
            val nowMs = System.currentTimeMillis()
            if (active) {
                aggregator.resumeRecording(nowMs)
            } else {
                aggregator.pauseRecording(nowMs)
            }
        }
    }

    private var analyzerScope: CoroutineScope? = null

    // Engines — created asynchronously on start(), closed on stop()
    @Volatile private var faceEngine: FaceLandmarkerEngine? = null
    @Volatile private var poseEngine: PoseLandmarkerEngine? = null
    @Volatile private var handEngine: HandLandmarkerEngine? = null
    @Volatile private var frameScheduler: FrameScheduler? = null

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
    private var currentLifecycleOwner: LifecycleOwner? = null
    private var currentImageAnalysis: ImageAnalysis? = null
    private var currentSurfaceProvider: Preview.SurfaceProvider? = null

    @Suppress("DEPRECATION")
    override fun start(
        cameraProvider: ProcessCameraProvider,
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider?,
    ) {
        if (_isRunning.getAndSet(true)) return

        this.cameraProvider = cameraProvider
        this.currentLifecycleOwner = lifecycleOwner
        _isRecordingActive.set(true)
        aggregator.reset()
        keyMomentDetector.reset()
        postureExtractor.reset()
        handExtractor.reset()
        lastFaceCenterNorm = null

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        this.analyzerScope = scope

        // Initialize heavy ML models asynchronously on background thread to prevent UI freezing
        scope.launch {
            try {
                val face = FaceLandmarkerEngine(context) { result, ts ->
                    val signal = faceExtractor.extract(result, ts)
                    if (_isRecordingActive.get()) {
                        aggregator.addFace(signal)
                        keyMomentDetector.onFaceFrame(signal)
                    }

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
                    if (_isRecordingActive.get()) {
                        aggregator.addPosture(signal)
                        keyMomentDetector.onPostureFrame(signal)
                    }
                }

                val hand = HandLandmarkerEngine(context) { result, ts ->
                    val signal = handExtractor.extract(result, ts, lastFaceCenterNorm)
                    if (_isRecordingActive.get()) {
                        aggregator.addHand(signal)
                        keyMomentDetector.onHandFrame(signal)
                    }
                }

                face.initialize()
                pose.initialize()
                hand.initialize()

                faceEngine = face
                poseEngine = pose
                handEngine = hand
                frameScheduler = FrameScheduler(face, pose, hand)

                Log.d(TAG, "MediaPipe body language engines initialized in background")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing MediaPipe engines", e)
            }
        }

        // CameraX ImageAnalysis (non-blocking)
        @Suppress("DEPRECATION")
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(480, 640))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()

        imageAnalysis.setAnalyzer(Dispatchers.Default.asExecutor()) { imageProxy ->
            try {
                val scheduler = frameScheduler
                if (scheduler != null) {
                    val bitmap = imageProxy.toBitmap()
                    val mpImage = BitmapImageBuilder(bitmap).build()
                    val timestampMs = TimeUnit.NANOSECONDS.toMillis(imageProxy.imageInfo.timestamp)
                    scheduler.onFrame(mpImage, timestampMs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing camera frame", e)
            } finally {
                imageProxy.close()
            }
        }

        this.currentImageAnalysis = imageAnalysis
        bindPreview(surfaceProvider)

        Log.d(TAG, "Body language camera analysis pipeline started")
    }

    override fun bindPreview(surfaceProvider: Preview.SurfaceProvider?) {
        this.currentSurfaceProvider = surfaceProvider
        val provider = cameraProvider ?: return
        val lifecycle = currentLifecycleOwner ?: return
        val analysis = currentImageAnalysis ?: return

        val preview = surfaceProvider?.let { sp ->
            Preview.Builder().build().also { previewUseCase ->
                previewUseCase.setSurfaceProvider(sp)
            }
        }

        try {
            provider.unbindAll()
            val useCases = listOfNotNull(analysis, preview).toTypedArray()
            provider.bindToLifecycle(
                lifecycle,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                *useCases,
            )
            Log.d(TAG, "Bound camera use cases (preview attached: ${surfaceProvider != null})")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind camera preview/analysis use cases", e)
        }
    }

    override fun stop() {
        if (!_isRunning.getAndSet(false)) return

        analyzerScope?.cancel()
        analyzerScope = null

        cameraProvider?.unbindAll()
        cameraProvider = null
        currentLifecycleOwner = null
        currentImageAnalysis = null
        currentSurfaceProvider = null

        val currentFace = faceEngine
        val currentPose = poseEngine
        val currentHand = handEngine

        faceEngine = null
        poseEngine = null
        handEngine = null
        frameScheduler = null

        CoroutineScope(Dispatchers.Default).launch {
            try {
                currentFace?.close()
                currentPose?.close()
                currentHand?.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing engines", e)
            }
        }

        Log.d(TAG, "Body language analysis stopped")
    }

    override suspend fun finalizeSession(): BodyLanguageMetrics =
        withContext(Dispatchers.Default) {
            val baseMetrics = aggregator.finalize()
            val keyMoments = keyMomentDetector.getKeyMoments()
            baseMetrics.copy(keyMoments = keyMoments)
        }
}
