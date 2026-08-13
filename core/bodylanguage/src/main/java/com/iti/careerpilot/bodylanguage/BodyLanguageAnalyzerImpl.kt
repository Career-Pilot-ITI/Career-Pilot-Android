package com.iti.careerpilot.bodylanguage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.iti.careerpilot.bodylanguage.aggregation.KeyMomentDetector
import com.iti.careerpilot.bodylanguage.aggregation.SessionAggregator
import com.iti.careerpilot.bodylanguage.engine.FaceLandmarkerEngine
import com.iti.careerpilot.bodylanguage.engine.FrameScheduler
import com.iti.careerpilot.bodylanguage.engine.HandLandmarkerEngine
import com.iti.careerpilot.bodylanguage.engine.PoseLandmarkerEngine
import com.iti.careerpilot.bodylanguage.signal.EyeContactApproximator
import com.iti.careerpilot.bodylanguage.signal.FaceSignalExtractor
import com.iti.careerpilot.bodylanguage.signal.HandSignalExtractor
import com.iti.careerpilot.bodylanguage.signal.PostureSignalExtractor
import com.iti.common.dispatcher.CareerPilotDispatchers.Default
import com.iti.common.dispatcher.CareerPilotDispatchers.IO
import com.iti.common.dispatcher.Dispatcher
import com.iti.core.model.bodylanguage.BodyLanguageMetrics
import dagger.hilt.android.qualifiers.ApplicationContext
import com.iti.common.dispatcher.di.ApplicationScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

private const val TAG = "BodyLanguageAnalyzer"

internal class BodyLanguageAnalyzerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @Dispatcher(Default) private val defaultDispatcher: CoroutineDispatcher,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
    @ApplicationScope private val appScope: CoroutineScope,
) : BodyLanguageAnalyzer {

    private val _isRunning = AtomicBoolean(false)
    override val isRunning: Boolean get() = _isRunning.get()

    private val _isRecordingActive = AtomicBoolean(false)
    override val isRecordingActive: Boolean get() = _isRecordingActive.get()

    override fun setRecordingActive(active: Boolean) {
        val wasActive = _isRecordingActive.getAndSet(active)
        if (wasActive != active) {
            val nowMs = SystemClock.elapsedRealtime()
            if (active) {
                aggregator.resumeRecording(nowMs)
            } else {
                aggregator.pauseRecording(nowMs)
            }
        }
    }

    override fun pauseRecording(timestampMs: Long) {
        _isRecordingActive.set(false)
        aggregator.pauseRecording(timestampMs)
    }

    override fun resumeRecording(timestampMs: Long) {
        _isRecordingActive.set(true)
        aggregator.resumeRecording(timestampMs)
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

    @Volatile
    private var lastFrameTimestampMs = 0L

    override fun start() {
        if (_isRunning.getAndSet(true)) return

        _isRecordingActive.set(false)
        lastFrameTimestampMs = 0L
        aggregator.reset()
        keyMomentDetector.reset()
        postureExtractor.reset()
        handExtractor.reset()
        lastFaceCenterNorm = null

        val scope = CoroutineScope(SupervisorJob() + defaultDispatcher)
        this.analyzerScope = scope

        // Initialize heavy ML models asynchronously on background thread to prevent UI freezing
        scope.launch(ioDispatcher) {
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

                face.initialize()

                if (!_isRunning.get()) {
                    try {
                        face.close()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error closing engines initialized after stop", e)
                    }
                    return@launch
                }

                faceEngine = face
                synchronized(this@BodyLanguageAnalyzerImpl) {
                    frameScheduler = FrameScheduler(face, poseEngine, handEngine)
                }

                Log.d(TAG, "MediaPipe body language engines initialized in background")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Error initializing MediaPipe engines", e)
            }
        }

        Log.d(TAG, "Body language camera analysis pipeline started")
    }

    override fun processImage(imageProxy: ImageProxy) {
        if (!_isRunning.get() || !_isRecordingActive.get()) {
            imageProxy.close()
            return
        }
        try {
            val scheduler = frameScheduler
            if (scheduler != null) {
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                val rawBitmap = imageProxy.toBitmap()
                val bitmap = if (rotationDegrees != 0) {
                    val matrix = Matrix().apply {
                        postRotate(rotationDegrees.toFloat())
                    }
                    Bitmap.createBitmap(
                        rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true
                    )
                } else {
                    rawBitmap
                }
                val mpImage = BitmapImageBuilder(bitmap).build()
                val now = SystemClock.elapsedRealtime()
                val timestampMs = synchronized(this) {
                    val ts = if (now <= lastFrameTimestampMs) lastFrameTimestampMs + 1 else now
                    lastFrameTimestampMs = ts
                    ts
                }
                scheduler.onFrame(mpImage, timestampMs)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing camera frame", e)
        } finally {
            imageProxy.close()
        }
    }

    override fun stop() {
        if (!_isRunning.getAndSet(false)) return

        analyzerScope?.cancel()
        analyzerScope = null

        val currentFace = faceEngine
        val currentPose = poseEngine
        val currentHand = handEngine

        faceEngine = null
        poseEngine = null
        handEngine = null
        frameScheduler = null

        appScope.launch(ioDispatcher) {
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

    override fun enablePostureTracking(enabled: Boolean) {
        if (enabled && poseEngine == null && _isRunning.get()) {
            analyzerScope?.launch(ioDispatcher) {
                try {
                    val pose = PoseLandmarkerEngine(context) { result, ts ->
                        val signal = postureExtractor.extract(result, ts)
                        if (_isRecordingActive.get()) {
                            aggregator.addPosture(signal)
                            keyMomentDetector.onPostureFrame(signal)
                        }
                    }
                    pose.initialize()
                    if (!_isRunning.get()) {
                        pose.close()
                        return@launch
                    }
                    poseEngine = pose
                    synchronized(this@BodyLanguageAnalyzerImpl) {
                        faceEngine?.let {
                            frameScheduler = FrameScheduler(it, poseEngine, handEngine)
                        }
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Log.e(TAG, "Error initializing pose engine", e)
                }
            }
        } else if (!enabled && poseEngine != null) {
            val pose = poseEngine
            poseEngine = null
            synchronized(this@BodyLanguageAnalyzerImpl) {
                faceEngine?.let { face ->
                    frameScheduler = FrameScheduler(face, null, handEngine)
                }
            }
            appScope.launch(ioDispatcher) {
                try {
                    pose?.close()
                } catch (e: Exception) {
                    Log.e(TAG, "Error closing pose engine", e)
                }
            }
        }
    }

    override fun enableHandTracking(enabled: Boolean) {
        if (enabled && handEngine == null && _isRunning.get()) {
            analyzerScope?.launch(ioDispatcher) {
                try {
                    val hand = HandLandmarkerEngine(context) { result, ts ->
                        val signal = handExtractor.extract(result, ts, lastFaceCenterNorm)
                        if (_isRecordingActive.get()) {
                            aggregator.addHand(signal)
                            keyMomentDetector.onHandFrame(signal)
                        }
                    }
                    hand.initialize()
                    if (!_isRunning.get()) {
                        hand.close()
                        return@launch
                    }
                    handEngine = hand
                    synchronized(this@BodyLanguageAnalyzerImpl) {
                        faceEngine?.let {
                            frameScheduler = FrameScheduler(it, poseEngine, handEngine)
                        }
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Log.e(TAG, "Error initializing hand engine", e)
                }
            }
        } else if (!enabled && handEngine != null) {
            val hand = handEngine
            handEngine = null
            synchronized(this@BodyLanguageAnalyzerImpl) {
                faceEngine?.let { face ->
                    frameScheduler = FrameScheduler(face, poseEngine, null)
                }
            }
            appScope.launch(ioDispatcher) {
                try {
                    hand?.close()
                } catch (e: Exception) {
                    Log.e(TAG, "Error closing hand engine", e)
                }
            }
        }
    }

    override suspend fun finalizeSession(): BodyLanguageMetrics =
        withContext(defaultDispatcher) {
            val baseMetrics = aggregator.finalize()
            val keyMoments = keyMomentDetector.getKeyMoments()
            baseMetrics.copy(keyMoments = keyMoments)
        }
}
