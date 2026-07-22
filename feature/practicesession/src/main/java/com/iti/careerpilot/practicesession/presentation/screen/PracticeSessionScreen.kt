package com.iti.careerpilot.practicesession.presentation.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.common.PermissionsDialog
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.core.designsystem.components.LoadingWave
import com.iti.careerpilot.practicesession.R
import com.iti.careerpilot.practicesession.presentation.action.PracticeSessionAction
import com.iti.careerpilot.practicesession.presentation.event.PracticeSessionEvent
import com.iti.careerpilot.practicesession.presentation.state.PracticeSessionState
import com.iti.careerpilot.practicesession.presentation.viewmodel.PracticeSessionViewModel
import com.iti.common.snackbar.CareerPilotSnackbarController

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
                showDiscardConfirm = true
            } else {
                onBack()
            }
        },
        onAction = viewModel::onAction
    )

    if (showDiscardConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirm = false },
            title = { Text(stringResource(R.string.leave_confirmation_title)) },
            text = { Text(stringResource(R.string.leave_confirmation_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardConfirm = false
                    viewModel.onAction(PracticeSessionAction.DiscardCurrentAnswerAndMakeNewOne)
                    onBack()
                }) { Text(stringResource(R.string.leave)) }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirm = false }) {
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
            onDismiss = {
                viewModel.onAction(PracticeSessionAction.ShowOrHidePermissionDialog(false))
            },
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
                        text = state.currentSession?.trackName ?: "",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        },
        bottomBar = { PracticeSessionBottomBar(state, onAction) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.isLoading && state.currentSession == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                QuestionSection(state, onAction)
                Spacer(modifier = Modifier.height(32.dp))

                AnimatedContent(
                    targetState = state.isRecording || state.recordedAudioPath != null,
                    label = "session_content"
                ) { isRecordingOrReviewing ->
                    if (isRecordingOrReviewing) {
                        RecordingOrReviewSection(state, onAction)
                    } else {
                        InitialSection(onAction)
                    }
                }
                if (state.isUploading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearWavyProgressIndicator(
                        progress = { state.uploadProgress / 100f },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun QuestionSection(state: PracticeSessionState, onAction: (PracticeSessionAction) -> Unit) {
    val question = state.currentSession?.currentQuestion
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Question ${question?.questionOrder ?: ""}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = question?.questionText ?: "Loading question...",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            minLines = 3
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    if (state.isReadingQuestion) onAction(PracticeSessionAction.PauseListeningToCurrentQuestion)
                    else onAction(PracticeSessionAction.ListenToAIReadingCurrentQuestion)
                }
            ) {
                Icon(
                    imageVector = if (state.isReadingQuestion) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Listen to question",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            if (state.isReadingQuestion) {
                LoadingWave(
                    modifier = Modifier.width(100.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun InitialSection(onAction: (PracticeSessionAction) -> Unit) {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp)
    ) {
        Text(
            text = "Ready to answer?",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        CareerPilotButton(
            text = "Start Answering",
            onClick = {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPermission) onAction(PracticeSessionAction.StartRecordingAnswer)
                else onAction(PracticeSessionAction.ShowOrHidePermissionDialog(true))
            },
            modifier = Modifier.fillMaxWidth(0.7f)
        )
    }
}

@Composable
fun RecordingOrReviewSection(
    state: PracticeSessionState,
    onAction: (PracticeSessionAction) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (state.isRecording) {
            Text(
                text = formatDuration(state.recordingDuration.inWholeMilliseconds),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(24.dp))
            WaveformView(amplitudes = state.amplitudes)
        } else if (state.recordedAudioPath != null) {
            Text(text = "Review your answer", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                IconButton(onClick = { onAction(PracticeSessionAction.PlayCurrentRecordedAnswer) }) {
                    Icon(
                        imageVector = if (state.isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                WaveformView(
                    amplitudes = state.amplitudes,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { onAction(PracticeSessionAction.DiscardCurrentAnswerAndMakeNewOne) },
                    // Disabled mid-submission so a slow tap can't yank the audio/transcript
                    // out from under an in-flight upload.
                    enabled = !state.isUploading && !state.isTranscribing
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun WaveformView(amplitudes: List<Float>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(60.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val lastAmplitudes = amplitudes.takeLast(40)
        lastAmplitudes.forEach { amplitude ->
            val height by animateFloatAsState(
                targetValue = amplitude.coerceIn(0.1f, 1f) * 60f,
                label = "wave"
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 1.dp)
                    .width(3.dp)
                    .height(height.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
fun PracticeSessionBottomBar(
    state: PracticeSessionState,
    onAction: (PracticeSessionAction) -> Unit,
) {
    BottomAppBar(
        containerColor = Color.Transparent,
        modifier = Modifier.padding(16.dp)
    ) {
        when {
            state.isRecording -> {
                CareerPilotButton(
                    text = "Finish Answer",
                    onClick = { onAction(PracticeSessionAction.FinishRecordingAnswerAndStartTranscription) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            state.recordedAudioPath != null -> {
                CareerPilotButton(
                    text = if (state.isUploading || state.isTranscribing) "Submitting..." else "Submit Answer",
                    onClick = { onAction(PracticeSessionAction.SubmitFinalAnswerToCurrentQuestion) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isUploading && !state.isTranscribing
                )
            }

            else -> Unit
        }
    }
}

private fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return "%02d:%02d".format(minutes, seconds)
}
