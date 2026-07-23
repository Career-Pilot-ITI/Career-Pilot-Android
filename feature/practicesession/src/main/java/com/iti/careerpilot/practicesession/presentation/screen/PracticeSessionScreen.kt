package com.iti.careerpilot.practicesession.presentation.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.common.GradientIcon
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.common.PermissionsDialog
import com.iti.careerpilot.practicesession.R
import com.iti.careerpilot.practicesession.presentation.action.PracticeSessionAction
import com.iti.careerpilot.practicesession.presentation.event.PracticeSessionEvent
import com.iti.careerpilot.practicesession.presentation.state.PracticeSessionState
import com.iti.careerpilot.practicesession.presentation.viewmodel.PracticeSessionViewModel
import com.iti.common.snackbar.CareerPilotSnackbarController
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun PracticeSessionRoot(
    trackId: Long,
    sessionId: Long? = null,
    onBack: () -> Unit,
    onNavigateToResult: (Long) -> Unit,
    viewModel: PracticeSessionViewModel = hiltViewModel()
) {
    ObserveEvent(viewModel.event) { newEvent ->
        when (newEvent) {
            is PracticeSessionEvent.NavigateToResult -> onNavigateToResult(newEvent.sessionId)
            is PracticeSessionEvent.ShowError -> {
                CareerPilotSnackbarController.show(message = newEvent.message)
            }
        }
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDiscardConfirm by remember { mutableStateOf(false) }
    var showSkipConfirm by remember { mutableStateOf(false) }
    var isLeaving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (sessionId != null && sessionId != 0L) {
            viewModel.onAction(PracticeSessionAction.RestartPracticeSession(sessionId))
        } else {
            viewModel.onAction(PracticeSessionAction.CreateNewPracticeSession(trackId))
        }
    }

    PracticeSessionScreen(
        state = state,
        onBack = {
            if (state.isRecording || state.recordedAudioPath != null) {
                isLeaving = true
                showDiscardConfirm = true
            } else {
                onBack()
            }
        },
        onAction = { action ->
            when (action) {
                is PracticeSessionAction.SkipCurrentQuestion -> showSkipConfirm = true
                is PracticeSessionAction.DiscardCurrentAnswer -> {
                    isLeaving = false
                    showDiscardConfirm = true
                }
                else -> viewModel.onAction(action)
            }
        }
    )

    if (showSkipConfirm) {
        AlertDialog(
            onDismissRequest = { showSkipConfirm = false },
            title = { Text("Skip Question?") },
            text = { Text("Are you sure you want to skip this question? You won't be able to answer it later in this session.") },
            confirmButton = {
                TextButton(onClick = {
                    showSkipConfirm = false
                    viewModel.onAction(PracticeSessionAction.SkipCurrentQuestion)
                }) { Text("Skip") }
            },
            dismissButton = {
                TextButton(onClick = { showSkipConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showDiscardConfirm) {
        AlertDialog(
            onDismissRequest = { 
                showDiscardConfirm = false
                isLeaving = false
            },
            title = { Text("Discard Recording?") },
            text = { Text("This will permanently delete your current recording.") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardConfirm = false
                    viewModel.onAction(PracticeSessionAction.DiscardCurrentAnswer)
                    if (isLeaving) onBack()
                    isLeaving = false
                }) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDiscardConfirm = false
                    isLeaving = false
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (state.showPermissionDialog) {
        PermissionsDialog(
            title = stringResource(R.string.mic_permission),
            text = stringResource(R.string.please_allow_microphone_permission_to_continue),
            icon = ImageVector.vectorResource(R.drawable.ic_mic),
            cancel = stringResource(R.string.cancel),
            allow = stringResource(R.string.allow),
            onDismiss = { viewModel.onAction(PracticeSessionAction.ShowOrHidePermissionDialog(false)) },
            onGranted = {
                viewModel.onAction(PracticeSessionAction.ShowOrHidePermissionDialog(false))
                viewModel.onAction(PracticeSessionAction.StartRecordingAnswer)
            },
            neededPermissions = arrayOf(Manifest.permission.RECORD_AUDIO)
        )
    }
}

@Composable
fun PracticeSessionScreen(
    state: PracticeSessionState,
    onBack: () -> Unit,
    onAction: (PracticeSessionAction) -> Unit,
) {
    BackHandler { onBack() }

    val processingPhase = remember(state.isUploading, state.isTranscribing, state.isLoadingNextQuestion) {
        when {
            state.isUploading -> ProcessingPhase.UPLOADING
            state.isTranscribing -> ProcessingPhase.TRANSCRIBING
            state.isLoadingNextQuestion -> ProcessingPhase.LOADING_NEXT
            else -> null
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                title = {
                    Text(
                        text = formatDuration(state.totalSessionDuration.inWholeMilliseconds),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        },
        bottomBar = {
            Column {
                AnimatedVisibility(visible = state.showQuestionCard) {
                    QuestionCard(state, onAction)
                }
                PracticeSessionBottomSection(state, onAction)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            if (state.isLoading && state.currentSession == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CenterStage(state = state)
                        Spacer(Modifier.height(16.dp))
                        AnimatedVisibility(visible = state.isRecording) {
                            Text(
                                text = formatDuration(state.recordingDuration.inWholeMilliseconds),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    ProcessingOverlay(phase = processingPhase, uploadProgress = state.uploadProgress)
}

// ─────────────────────────────────────────────────────────────────────────
// Center stage: siri-like orb + rotating amplitude rings
// ─────────────────────────────────────────────────────────────────────────

@Composable
fun CenterStage(state: PracticeSessionState, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(260.dp), contentAlignment = Alignment.Center) {
        if (state.isRecording) {
            AmplitudeRings(
                amplitudes = state.amplitudes,
                modifier = Modifier.fillMaxSize()
            )
        }
        AiTalkingAnimation(
            isPulsing = state.isReadingQuestion,
            onClick = {
                // todo start and stop the animation and the text to speech
            }
        )
        val icon = when {
            state.isRecording -> Icons.Default.Mic
            state.recordedAudioPath != null -> Icons.Default.GraphicEq
            state.isReadingQuestion -> Icons.AutoMirrored.Filled.VolumeUp
            else -> Icons.Default.RecordVoiceOver
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(42.dp)
        )
    }
}


/**
 * Three rings of bars orbiting the orb, each ring reading a different slice
 * of the recent amplitude history, rotating at different speeds/directions.
 */
@Composable
private fun AmplitudeRings(amplitudes: List<Float>, modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "rings")
    val rotation1 by infinite.animateFloat(
        0f, 360f, infiniteRepeatable(tween(14000, easing = LinearEasing)), label = "rot1"
    )
    val rotation2 by infinite.animateFloat(
        360f, 0f, infiniteRepeatable(tween(10000, easing = LinearEasing)), label = "rot2"
    )
    val rotation3 by infinite.animateFloat(
        0f, 360f, infiniteRepeatable(tween(18000, easing = LinearEasing)), label = "rot3"
    )

    val recent = amplitudes.takeLast(48).ifEmpty { List(48) { 0.05f } }
    val ringA = recent.filterIndexed { i, _ -> i % 3 == 0 }.ifEmpty { listOf(0.05f) }
    val ringB = recent.filterIndexed { i, _ -> i % 3 == 1 }.ifEmpty { listOf(0.05f) }
    val ringC = recent.filterIndexed { i, _ -> i % 3 == 2 }.ifEmpty { listOf(0.05f) }

    val barColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val ringConfigs = listOf(
            Triple(ringA, size.minDimension * 0.30f, rotation1),
            Triple(ringB, size.minDimension * 0.38f, rotation2),
            Triple(ringC, size.minDimension * 0.46f, rotation3)
        )
        ringConfigs.forEach { (values, radius, rot) ->
            val count = values.size
            rotate(rot) {
                for (i in 0 until count) {
                    val angle = (2 * PI * i / count).toFloat()
                    val amp = values[i].coerceIn(0.05f, 1f)
                    val barLen = 5.dp.toPx() + amp * 16.dp.toPx()
                    val start = Offset(
                        center.x + cos(angle) * radius,
                        center.y + sin(angle) * radius
                    )
                    val end = Offset(
                        center.x + cos(angle) * (radius + barLen),
                        center.y + sin(angle) * (radius + barLen)
                    )
                    drawLine(
                        color = barColor.copy(alpha = 0.5f + amp * 0.5f),
                        start = start,
                        end = end,
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
fun PracticeSessionBottomSection(state: PracticeSessionState, onAction: (PracticeSessionAction) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            if (state.recordedAudioPath != null && !state.isRecording) {
                AudioReviewRow(state, onAction)
            } else {
                ActionBottomBar(state, onAction)
            }
        }
    }
}

@Composable
private fun AudioReviewRow(state: PracticeSessionState, onAction: (PracticeSessionAction) -> Unit) {
    val busy = state.isUploading || state.isTranscribing || state.isLoadingNextQuestion
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                modifier = Modifier.align(Alignment.TopStart),
                onClick = { onAction(PracticeSessionAction.DiscardCurrentAnswer) },
                enabled = !busy
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete recording",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(onClick = { onAction(PracticeSessionAction.PlayCurrentRecordedAnswer) }) {
                Icon(
                    imageVector = if (state.isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (state.isPlayingAudio) "Pause" else "Play"
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val progress = if (state.playbackDurationMs > 0) {
                    (state.playbackPositionMs.toFloat() / state.playbackDurationMs).coerceIn(0f, 1f)
                } else 0f
                WavySeekSlider(
                    progress = progress,
                    isPlaying = state.isPlayingAudio,
                    onSeek = { p: Float ->
                        val targetMs = (p * state.playbackDurationMs).toLong()
                        onAction(PracticeSessionAction.SeekAudioTo(targetMs))
                    }
                )
                Spacer(Modifier.height(2.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = formatDuration(state.playbackPositionMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDuration(state.playbackDurationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            LargeGradientIconButton(
                icon = Icons.AutoMirrored.Rounded.Send,
                onClick = { onAction(PracticeSessionAction.SubmitFinalAnswerToCurrentQuestion) },
                enabled = !busy,
                size = 56.dp
            )
        }
    }
}

@Composable
private fun ActionBottomBar(state: PracticeSessionState, onAction: (PracticeSessionAction) -> Unit) {
    val context = LocalContext.current
    val busy = state.isUploading || state.isTranscribing || state.isLoadingNextQuestion

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Skip Button
        IconButton(
            onClick = { onAction(PracticeSessionAction.SkipCurrentQuestion) },
            enabled = !busy && !state.isRecording
        ) {
            Icon(
                Icons.Default.SkipNext,
                contentDescription = "Skip question",
                modifier = Modifier.size(28.dp)
            )
        }

        // Center Mic/Stop Button
        val icon = if (state.isRecording) Icons.Default.Stop else Icons.Default.Mic
        val onClick = {
            if (state.isRecording) {
                onAction(PracticeSessionAction.FinishRecordingAnswerAndStartTranscription)
            } else {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPermission) onAction(PracticeSessionAction.StartRecordingAnswer)
                else onAction(PracticeSessionAction.ShowOrHidePermissionDialog(true))
            }
        }

        Box(contentAlignment = Alignment.Center) {
            if (state.isRecording) {
                WavyBorder(amplitudes = state.amplitudes)
            }
            LargeGradientIconButton(
                icon = icon,
                onClick = onClick,
                enabled = !busy,
                size = 80.dp,
                isOutlined = true
            )
        }

        // Toggle Question Card
        IconButton(
            onClick = { onAction(PracticeSessionAction.ToggleQuestionCard) },
            enabled = !busy && !state.isRecording
        ) {
            Icon(
                imageVector = if (state.showQuestionCard) Icons.Default.Description else Icons.Default.GraphicEq,
                contentDescription = "Toggle question card",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun LargeGradientIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
    size: androidx.compose.ui.unit.Dp = 72.dp,
    isOutlined: Boolean = false
) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.secondary
        )
    )

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (isOutlined) Modifier.background(Color.Transparent)
                else Modifier.background(gradientBrush)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .then(
                if (isOutlined) Modifier.background(Color.Transparent).padding(2.dp).background(MaterialTheme.colorScheme.surface, CircleShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isOutlined) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = gradientBrush,
                    radius = (size.toPx() / 2) - 1.dp.toPx(),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
            GradientIcon(icon = icon, modifier = Modifier.size(size * 0.5f))
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
}

@Composable
fun WavyBorder(amplitudes: List<Float>) {
    val infiniteTransition = rememberInfiniteTransition(label = "wavy_border")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val recentAmps = amplitudes.takeLast(10).ifEmpty { listOf(0.1f) }
    val avgAmp = recentAmps.average().toFloat().coerceIn(0.1f, 1f)

    Canvas(modifier = Modifier.size(100.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val baseRadius = size.width / 2 - 5.dp.toPx()
        val path = Path()
        
        val points = 100
        val random = Random(42) // Fixed seed for some consistency but random-looking
        for (i in 0 until points) {
            val angle = (i.toFloat() / points) * 2 * PI.toFloat()
            
            // Combine multiple sine waves with different frequencies and phases for a "random" organic feel
            val wave1 = sin(angle * 6 + phase) * 6.dp.toPx()
            val wave2 = sin(angle * 14 - phase * 1.2f) * 4.dp.toPx()
            val wave3 = cos(angle * 8 + phase * 0.8f) * 3.dp.toPx()
            
            val noise = wave1 + wave2 + wave3
            val r = baseRadius + noise * avgAmp
            val x = center.x + r * cos(angle)
            val y = center.y + r * sin(angle)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        drawPath(
            path = path,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF6D5DF6),
                    Color(0xFF9D5CF9),
                    Color(0xFF3ED6C6)
                )
            ),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────
// Processing overlay dialog (uploading -> transcribing -> loading next)
// ─────────────────────────────────────────────────────────────────────────

private enum class ProcessingPhase(val label: String, val icon: ImageVector) {
    UPLOADING("Uploading your answer...", Icons.Default.CloudUpload),
    TRANSCRIBING("Transcribing your response...", Icons.Default.Description),
    LOADING_NEXT("Getting your next question...", Icons.Default.RecordVoiceOver)
}

@Composable
private fun ProcessingOverlay(phase: ProcessingPhase?, uploadProgress: Int) {
    if (phase == null) return

    Dialog(
        onDismissRequest = { /* not dismissable while processing */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center
        ) {
            ElevatedCard(shape = RoundedCornerShape(28.dp)) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 32.dp, vertical = 28.dp)
                        .widthIn(min = 220.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedContent(
                        targetState = phase,
                        transitionSpec = {
                            (fadeIn() + scaleIn(initialScale = 0.85f)) togetherWith
                                    (fadeOut() + scaleOut(targetScale = 1.15f))
                        },
                        label = "phase_icon"
                    ) { p ->
                        Icon(
                            imageVector = p.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    AnimatedContent(targetState = phase, label = "phase_text") { p ->
                        Text(
                            text = p.label,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    if (phase == ProcessingPhase.UPLOADING) {
                        LinearWavyProgressIndicator(
                            progress = { uploadProgress / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "$uploadProgress%",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    Spacer(Modifier.height(18.dp))
                    StepDots(current = phase)
                }
            }
        }
    }
}

@Composable
private fun StepDots(current: ProcessingPhase) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ProcessingPhase.entries.forEach { step ->
            val active = step.ordinal <= current.ordinal
            val color by animateColorAsStateCompat(
                target = if (active) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun animateColorAsStateCompat(target: Color) =
    animateColorAsState(targetValue = target, label = "color")

@Composable
private fun WavySeekSlider(
    progress: Float,
    isPlaying: Boolean,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "wavy_seek")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "wave_phase"
    )
    var dragProgress by remember { mutableStateOf<Float?>(null) }
    val shownProgress = dragProgress ?: progress

    val trackColor = MaterialTheme.colorScheme.primary
    val trackBg = MaterialTheme.colorScheme.surfaceVariant

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset -> dragProgress = (offset.x / size.width).coerceIn(0f, 1f) },
                    onDrag = { change, _ -> dragProgress = (change.position.x / size.width).coerceIn(0f, 1f) },
                    onDragEnd = {
                        dragProgress?.let(onSeek)
                        dragProgress = null
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onSeek((offset.x / size.width).coerceIn(0f, 1f))
                }
            }
    ) {
        val midY = size.height / 2
        val width = size.width

        drawLine(
            color = trackBg,
            start = Offset(0f, midY),
            end = Offset(width, midY),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )

        val filledWidth = width * shownProgress
        val amplitude = if (isPlaying) 6.dp.toPx() else 0f
        val steps = 60
        val path = Path()
        for (i in 0..steps) {
            val x = filledWidth * i / steps
            val y = midY + sin(phase + i * 0.55f) * amplitude
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path = path, color = trackColor, style = Stroke(
            width = 4.dp.toPx(),
            cap = StrokeCap.Round
        )
        )

        val thumbY = midY + sin(phase + steps * 0.55f) * amplitude
        drawCircle(color = trackColor, radius = 7.dp.toPx(), center = Offset(filledWidth, thumbY))
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Composable
fun QuestionCard(state: PracticeSessionState, onAction: (PracticeSessionAction) -> Unit) {
    val question = state.currentSession?.currentQuestion
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question ${question?.questionOrder ?: ""}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Row {
                    IconButton(onClick = { onAction(PracticeSessionAction.ListenToAIReadingCurrentQuestion) }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                    }
                    IconButton(onClick = { onAction(PracticeSessionAction.PauseListeningToCurrentQuestion) }) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop")
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = question?.questionText ?: "Loading question...",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                maxLines = 4
            )
        }
    }
}
