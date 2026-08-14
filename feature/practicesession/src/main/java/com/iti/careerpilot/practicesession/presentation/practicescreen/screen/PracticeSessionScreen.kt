package com.iti.careerpilot.practicesession.presentation.practicescreen.screen

import android.Manifest
import android.content.pm.PackageManager
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.common.PermissionsDialog
import com.iti.careerpilot.core.designsystem.components.BackIconButton
import com.iti.careerpilot.practicesession.R
import com.iti.careerpilot.practicesession.presentation.practicescreen.action.PracticeSessionAction
import com.iti.careerpilot.practicesession.presentation.practicescreen.event.PracticeSessionEvent
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components.AmbientStageBackdrop
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components.CameraPreviewPip
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components.CenterStage
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components.ConfirmationDialog
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components.PracticeSessionBottomSection
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components.PracticeSessionSettingsBottomSheet
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components.ProcessingDialog
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components.QuestionCard
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components.RecordingDurationCard
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components.RecordingWave
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components.SessionLoadingDialog
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components.TopErrorNotification
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.util.formatDuration
import com.iti.careerpilot.practicesession.presentation.practicescreen.state.PracticeSessionState
import com.iti.careerpilot.practicesession.presentation.practicescreen.viewmodel.PracticeSessionViewModel
import com.iti.common.util.UIText
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun PracticeSessionRoot(
    trackId: Long,
    sessionId: Long? = null,
    isVideoSession: Boolean = false,
    enablePostureTracking: Boolean = false,
    enableHandTracking: Boolean = false,
    onBack: () -> Unit,
    onNavigateToResult: (Long) -> Unit,
    viewModel: PracticeSessionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<UIText?>(null) }
    val view = LocalView.current

    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowInsetsControllerCompat(window, view)
            insetsController.isAppearanceLightNavigationBars = false
        }
        onDispose {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(window, true)
            }
        }
    }

    ObserveEvent(viewModel.event) { newEvent ->
        when (newEvent) {
            is PracticeSessionEvent.NavigateToResult -> onNavigateToResult(newEvent.sessionId)
            is PracticeSessionEvent.ShowError -> {
                errorMessage = newEvent.message
            }
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            delay(3000L.milliseconds)
            errorMessage = null
        }
    }

    LaunchedEffect(Unit) {
        if (sessionId != null && sessionId != 0L) {
            viewModel.onAction(
                PracticeSessionAction.RestartPracticeSession(
                    sessionId = sessionId,
                    isVideoSession = isVideoSession,
                    enablePostureTracking = enablePostureTracking,
                    enableHandTracking = enableHandTracking
                )
            )
        } else {
            viewModel.onAction(
                PracticeSessionAction.CreateNewPracticeSession(
                    trackId = trackId,
                    isVideoSession = isVideoSession,
                    enablePostureTracking = enablePostureTracking,
                    enableHandTracking = enableHandTracking
                )
            )
        }
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        PracticeSessionScreen(
            state = state,
            onBack = {
                viewModel.onAction(PracticeSessionAction.ShowOrHideLeaveConfirmDialog(true))
            },
            onAction = viewModel::onAction,
        )

        AnimatedVisibility(
            visible = errorMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            errorMessage?.let {
                TopErrorNotification(
                    message = it,
                    onDismiss = { errorMessage = null }
                )
            }
        }
    }

    if (state.showSettingsBottomSheet) {
        PracticeSessionSettingsBottomSheet(
            autoReadQuestion = state.autoReadQuestion,
            showCameraPreviewToggle = state.isBodyLanguageAnalyzing,
            isCameraPreviewVisible = state.isCameraPreviewVisible,
            onAutoReadToggle = { viewModel.onAction(PracticeSessionAction.ToggleAutoReadQuestion(it)) },
            onCameraPreviewToggle = { viewModel.onAction(PracticeSessionAction.ToggleCameraPreview(it)) },
            onDismiss = {
                viewModel.onAction(
                    PracticeSessionAction.ShowOrHideSettingsBottomSheet(
                        false
                    )
                )
            }
        )
    }

    if (state.showLeaveConfirm) {
        ConfirmationDialog(
            title = stringResource(R.string.leave_confirmation_title),
            text = stringResource(R.string.leave_confirmation_message),
            icon = ImageVector.vectorResource(R.drawable.ic_logout),
            onDismiss = {
                viewModel.onAction(PracticeSessionAction.ShowOrHideLeaveConfirmDialog(false))
            },
            onConfirm = { onBack() },
            cancel = stringResource(R.string.cancel),
            confirm = stringResource(R.string.leave)
        )
    }

    if (state.showDiscardConfirm) {
        ConfirmationDialog(
            title = stringResource(R.string.discard_recording_title),
            text = stringResource(R.string.discard_recording_message),
            icon = ImageVector.vectorResource(R.drawable.ic_delete),
            onDismiss = {
                viewModel.onAction(
                    PracticeSessionAction.ShowOrHideDiscardConfirmDialog(
                        false
                    )
                )
            },
            onConfirm = { viewModel.onAction(PracticeSessionAction.DiscardCurrentAnswer) },
            cancel = stringResource(R.string.cancel),
            confirm = stringResource(R.string.discard)
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

    if (state.isLoadingSession && !state.isUploadingAndTranscribingAudio && !state.isSendingAnswer) {
        SessionLoadingDialog()
    }
    if (state.isUploadingAndTranscribingAudio ||
        state.isSendingAnswer
    ) {
        ProcessingDialog(
            uploadProgress = state.uploadProgress,
            isSendingAnswer = state.isSendingAnswer,
            isEmptyAnswer = state.isEmptyAnswer,
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
    val isCameraPreviewVisible = state.isBodyLanguageAnalyzing && state.isCameraPreviewVisible

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AmbientStageBackdrop()

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        BackIconButton(onBack = onBack)
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (state.isRecording) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.error)
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(
                                text = formatDuration(state.totalSessionDuration.inWholeMilliseconds),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            bottomBar = {
                PracticeSessionBottomSection(
                    isRecording = state.isRecording,
                    recordedAudioPath = state.recordedAudioPath,
                    isPlayingAudio = state.isPlayingAudio,
                    playbackDurationMs = state.playbackDurationMs,
                    playbackPositionMs = state.playbackPositionMs,
                    showQuestionCard = state.showQuestionCard,
                    onAction = onAction,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            },
            containerColor = Color.Transparent,
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main Stage Area (Camera Preview in Video Mode, CenterStage in Audio Mode)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 340.dp)
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCameraPreviewVisible) {
                        CameraPreviewPip(
                            isVisible = true,
                            isRecording = state.isRecording,
                            onFrame = { imageProxy ->
                                onAction(PracticeSessionAction.OnFrame(imageProxy))
                            },
                            modifier = Modifier.fillMaxSize(),
                            previewModifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(24.dp),
                            borderCornerRadius = 24.dp,
                        )
                    } else {
                        CenterStage(
                            isReadingQuestion = state.isReadingQuestion,
                            isRecording = state.isRecording,
                            onToggleListening = {
                                if (state.isReadingQuestion) {
                                    onAction(PracticeSessionAction.PauseListeningToCurrentQuestion)
                                } else {
                                    onAction(PracticeSessionAction.ListenToAIReadingCurrentQuestion)
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Question Card (positioned below the camera stage)
                AnimatedVisibility(
                    visible = state.showQuestionCard,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    QuestionCard(
                        questionOrder = state.currentSession?.currentQuestion?.questionOrder,
                        questionText = state.currentSession?.currentQuestion?.questionText,
                        isReadingQuestion = state.isReadingQuestion,
                        onPlayClick = { onAction(PracticeSessionAction.ListenToAIReadingCurrentQuestion) },
                        onStopClick = { onAction(PracticeSessionAction.PauseListeningToCurrentQuestion) },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Recording Waveform & Duration
                AnimatedVisibility(
                    visible = state.isRecording,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        RecordingWave(
                            volumeBars = state.volumeBars,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        RecordingDurationCard(
                            durationMs = state.recordingDuration.inWholeMilliseconds,
                            isRecording = state.isRecording
                        )
                    }
                }
            }
        }
    }
}

