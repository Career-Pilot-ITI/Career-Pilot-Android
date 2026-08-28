package com.iti.careerpilot.companyinterview.presentation.screen.components

import android.util.Log
import android.util.Size
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Composable
fun CameraProctoringPreview(
    onFrame: (ImageProxy) -> Unit,
    modifier: Modifier = Modifier,
    isRecording: Boolean = false,
    eyeContactScore: Float = 100f
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var surfaceRequest by remember { mutableStateOf<SurfaceRequest?>(null) }

    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            analysisExecutor.shutdown()
        }
    }

    LaunchedEffect(lifecycleOwner) {
        val cameraProvider = context.getCameraProvider()

        val resolutionSelector = ResolutionSelector.Builder()
            .setResolutionStrategy(
                ResolutionStrategy(
                    Size(480, 640),
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                )
            )
            .build()

        val preview = Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .build()
            .apply {
                setSurfaceProvider { request ->
                    surfaceRequest = request
                }
            }

        val imageAnalysis = ImageAnalysis.Builder()
            .setResolutionSelector(resolutionSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .apply {
                setAnalyzer(analysisExecutor) { imageProxy ->
                    onFrame(imageProxy)
                }
            }

        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
        } catch (e: Exception) {
            Log.e("CameraProctoringPreview", "Use case binding failed", e)
        }
    }

    Box(
        modifier = modifier
            .size(width = 130.dp, height = 175.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 2.dp,
                color = if (isRecording) MaterialTheme.colorScheme.primary else Color.LightGray,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        surfaceRequest?.let { request ->
            CameraXViewfinder(
                surfaceRequest = request,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Live Gaze Badge
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = if (isRecording) Color(0xFF22C55E) else Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = if (eyeContactScore > 75) "Gaze OK" else "Look At Camera",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private suspend fun android.content.Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProviderFuture ->
            cameraProviderFuture.addListener(
                {
                    try {
                        continuation.resume(cameraProviderFuture.get())
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                },
                ContextCompat.getMainExecutor(this)
            )
        }
    }
