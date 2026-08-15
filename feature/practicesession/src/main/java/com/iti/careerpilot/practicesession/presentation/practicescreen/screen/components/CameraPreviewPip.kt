package com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components

import android.graphics.Matrix
import android.util.Log
import android.util.Size
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SweepGradientShader
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val TAG = "CameraPreviewPip"

@Composable
fun CameraPreviewPip(
    isVisible: Boolean,
    onFrame: (ImageProxy) -> Unit,
    modifier: Modifier = Modifier,
    previewModifier: Modifier = Modifier.size(width = 100.dp, height = 140.dp),
    shape: Shape = RoundedCornerShape(12.dp),
    borderCornerRadius: Dp = 12.dp,
    isRecording: Boolean = false,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var surfaceRequest by remember { mutableStateOf<SurfaceRequest?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "pip_border_rotation")
    val angle = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pip_border_angle",
    )

    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val secondary = MaterialTheme.colorScheme.secondary

    val gradientColors = remember(primary, tertiary, secondary) {
        listOf(primary, tertiary, secondary, primary)
    }

    val rotatingBrush = remember(gradientColors) {
        object : ShaderBrush() {
            var currentAngle: Float = 0f
            private var cachedShader: Shader? = null
            private var cachedWidth: Float = -1f
            private var cachedHeight: Float = -1f
            private val matrix = Matrix()

            override fun createShader(size: androidx.compose.ui.geometry.Size): Shader {
                val shader = if (cachedShader == null || cachedWidth != size.width || cachedHeight != size.height) {
                    cachedWidth = size.width
                    cachedHeight = size.height
                    val center = Offset(size.width / 2f, size.height / 2f)
                    SweepGradientShader(center = center, colors = gradientColors).also {
                        cachedShader = it
                    }
                } else {
                    cachedShader!!
                }
                matrix.reset()
                matrix.postRotate(currentAngle, size.width / 2f, size.height / 2f)
                shader.setLocalMatrix(matrix)
                return shader
            }
        }
    }

    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    val borderModifier = if (isRecording) {
        val currentAngle = angle.value
        Modifier.drawWithContent {
            drawContent()
            rotatingBrush.currentAngle = currentAngle
            drawRoundRect(
                brush = rotatingBrush,
                cornerRadius = CornerRadius(borderCornerRadius.toPx(), borderCornerRadius.toPx()),
                style = Stroke(width = 3.dp.toPx()),
            )
        }
    } else {
        Modifier.drawWithContent {
            drawContent()
            drawRoundRect(
                color = outlineColor,
                cornerRadius = CornerRadius(borderCornerRadius.toPx(), borderCornerRadius.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }

    LaunchedEffect(lifecycleOwner) {
        val cameraExecutor = Executors.newSingleThreadExecutor()
        val cameraProvider = ProcessCameraProvider.getInstance(context).await(context)

        val previewUseCase = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider { request ->
                    surfaceRequest = request
                }
            }

        val resolutionSelector = ResolutionSelector.Builder()
            .setResolutionStrategy(
                ResolutionStrategy(
                    Size(320, 240),
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                )
            )
            .build()

        val imageAnalysisUseCase = ImageAnalysis.Builder()
            .setResolutionSelector(resolutionSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()

        imageAnalysisUseCase.setAnalyzer(cameraExecutor) { imageProxy ->
            onFrame(imageProxy)
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                previewUseCase,
                imageAnalysisUseCase,
            )
            awaitCancellation()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind camera use cases", e)
        } finally {
            cameraProvider.unbindAll()
            cameraExecutor.shutdown()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        surfaceRequest?.let { request ->
            CameraXViewfinder(
                surfaceRequest = request,
                modifier = previewModifier
                    .clip(shape)
                    .then(borderModifier),
            )
        }
    }
}

private suspend fun <T> ListenableFuture<T>.await(context: android.content.Context): T =
    suspendCancellableCoroutine { cont ->
        addListener({
            try {
                cont.resume(get())
            } catch (e: Exception) {
                cont.resumeWithException(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }
