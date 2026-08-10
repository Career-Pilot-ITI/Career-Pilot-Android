package com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components

import android.graphics.Matrix
import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executors

private const val TAG = "CameraPreviewPip"

@Composable
fun CameraPreviewPip(
    isVisible: Boolean,
    onFrame: (ImageProxy) -> Unit,
    modifier: Modifier = Modifier,
    previewModifier: Modifier = Modifier.size(width = 100.dp, height = 140.dp),
    shape: Shape = RoundedCornerShape(12.dp),
    borderCornerRadius: Dp = 12.dp,
    showAnimatedBorder: Boolean = true,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

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

    val borderModifier = if (showAnimatedBorder) {
        Modifier.drawWithContent {
            drawContent()
            rotatingBrush.currentAngle = angle.value
            drawRoundRect(
                brush = rotatingBrush,
                cornerRadius = CornerRadius(borderCornerRadius.toPx(), borderCornerRadius.toPx()),
                style = Stroke(width = 3.dp.toPx()),
            )
        }
    } else {
        Modifier
    }

    DisposableEffect(lifecycleOwner, previewView) {
        val currentPreviewView = previewView ?: return@DisposableEffect onDispose {}
        val cameraExecutor = Executors.newSingleThreadExecutor()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        var cameraProvider: ProcessCameraProvider? = null

        val listener = Runnable {
            try {
                val provider = cameraProviderFuture.get()
                cameraProvider = provider

                val previewUseCase = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(currentPreviewView.surfaceProvider)
                    }

                @Suppress("DEPRECATION")
                val imageAnalysisUseCase = ImageAnalysis.Builder()
                    .setTargetResolution(Size(480, 640))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build()

                imageAnalysisUseCase.setAnalyzer(cameraExecutor) { imageProxy ->
                    onFrame(imageProxy)
                }

                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    previewUseCase,
                    imageAnalysisUseCase,
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to bind camera use cases", e)
            }
        }

        cameraProviderFuture.addListener(listener, ContextCompat.getMainExecutor(context))

        onDispose {
            try {
                cameraProvider?.unbindAll()
            } catch (e: Exception) {
                Log.e(TAG, "Error unbinding camera provider", e)
            }
            cameraExecutor.shutdown()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    previewView = this
                }
            },
            modifier = previewModifier
                .clip(shape)
                .then(borderModifier),
        )
    }
}
