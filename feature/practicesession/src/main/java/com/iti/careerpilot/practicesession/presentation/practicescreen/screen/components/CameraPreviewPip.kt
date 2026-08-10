package com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components

import android.graphics.Matrix
import androidx.camera.core.Preview
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SweepGradientShader
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CameraPreviewPip(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    previewModifier: Modifier = Modifier.size(width = 100.dp, height = 140.dp),
    shape: Shape = RoundedCornerShape(12.dp),
    borderCornerRadius: Dp = 12.dp,
    showAnimatedBorder: Boolean = true,
    onSurfaceProviderReady: (Preview.SurfaceProvider) -> Unit,
) {
    val context = LocalContext.current
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val borderModifier = if (showAnimatedBorder) {
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

        val gradientColors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.primary,
        )

        val matrix = remember { Matrix() }

        Modifier.drawWithContent {
            drawContent()
            val center = Offset(size.width / 2f, size.height / 2f)
            val shader = SweepGradientShader(
                center = center,
                colors = gradientColors,
            )
            matrix.reset()
            matrix.postRotate(angle.value, center.x, center.y)
            shader.setLocalMatrix(matrix)
            val rotatingShaderBrush = ShaderBrush(shader)

            drawRoundRect(
                brush = rotatingShaderBrush,
                cornerRadius = CornerRadius(borderCornerRadius.toPx(), borderCornerRadius.toPx()),
                style = Stroke(width = 3.dp.toPx()),
            )
        }
    } else {
        Modifier
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        AndroidView(
            factory = {
                onSurfaceProviderReady(previewView.surfaceProvider)
                previewView
            },
            modifier = previewModifier
                .clip(shape)
                .then(borderModifier),
        )
    }
}
