package com.iti.careerpilot.practicesession.presentation.practicescreen.screen

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.common.PermissionsDialog
import com.iti.careerpilot.core.designsystem.components.LoadingDialog
import com.iti.careerpilot.practicesession.R
import com.iti.careerpilot.practicesession.presentation.practicescreen.action.PracticeSessionAction
import com.iti.careerpilot.practicesession.presentation.practicescreen.event.PracticeSessionEvent
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components.AmbientStageBackdrop
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components.BodyLanguageConsentDialog
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components.CameraPreviewPip
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components.CenterStage
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components.ConfirmationDialog
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components.PracticeSessionBottomSection
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components.PracticeSessionSettingsBottomSheet
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components.ProcessingDialog
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components.QuestionCard
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components.RecordingDurationCard
import com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components.RecordingWave
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
    onBack: () -> Unit,
    onNavigateToResult: (Long, String?) -> Unit,
    viewModel: PracticeSessionViewModel = hiltViewModel()
) {
    var errorMessage by remember { mutableStateOf<UIText?>(null) }
    var shouldRequestCameraPermission by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    ObserveEvent(viewModel.event) { newEvent ->
        when (newEvent) {
            is PracticeSessionEvent.NavigateToResult -> onNavigateToResult(newEvent.sessionId, newEvent.bodyLanguageMetricsJson)
            is PracticeSessionEvent.ShowError -> {
                errorMessage = newEvent.message
            }
            is PracticeSessionEvent.RequestCameraPermission -> {
                shouldRequestCameraPermission = true
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
            viewModel.onAction(PracticeSessionAction.RestartPracticeSession(sessionId))
        } else {
            viewModel.onAction(PracticeSessionAction.CreateNewPracticeSession(trackId))
        }
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        PracticeSessionScreen(
            state = state,
            onBack = {
                viewModel.onAction(PracticeSessionAction.ShowOrHideLeaveConfirmDialog(true))
            },
            onAction = viewModel::onAction
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

        // Camera preview PiP overlay (top-end corner)
        if (state.isBodyLanguageAnalyzing) {
            CameraPreviewPip(
                isVisible = state.isCameraPreviewVisible,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                onSurfaceProviderReady = { surfaceProvider ->
                    viewModel.onAction(PracticeSessionAction.OnSurfaceProviderReady(surfaceProvider))
                },
            )
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

    if (shouldRequestCameraPermission) {
        PermissionsDialog(
            title = stringResource(R.string.camera_permission),
            text = stringResource(R.string.please_allow_camera_permission_for_body_language),
            icon = ImageVector.vectorResource(R.drawable.ic_camera),
            cancel = stringResource(R.string.cancel),
            allow = stringResource(R.string.allow),
            onDismiss = {
                shouldRequestCameraPermission = false
                viewModel.onAction(PracticeSessionAction.OnCameraPermissionResult(false))
            },
            onGranted = {
                shouldRequestCameraPermission = false
                viewModel.onAction(PracticeSessionAction.OnCameraPermissionResult(true))

                // Obtain camera provider and send to ViewModel for body language analysis
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener(
                    {
                        val cameraProvider = cameraProviderFuture.get()
                        viewModel.onAction(
                            PracticeSessionAction.OnCameraProviderReady(
                                cameraProvider = cameraProvider,
                                lifecycleOwner = lifecycleOwner,
                                surfaceProvider = null, // Surface provided later by CameraPreviewPip
                            )
                        )
                    },
                    ContextCompat.getMainExecutor(context)
                )
            },
            neededPermissions = arrayOf(Manifest.permission.CAMERA)
        )
    }

    // Body language consent dialog
    if (state.showBodyLanguageConsentDialog) {
        BodyLanguageConsentDialog(
            onAccept = { viewModel.onAction(PracticeSessionAction.AcceptBodyLanguageConsent) },
            onDecline = { viewModel.onAction(PracticeSessionAction.DeclineBodyLanguageConsent) },
        )
    }

    if (state.isLoadingSession) {
        LoadingDialog(
            title = stringResource(R.string.loading_session),
        )
    }
    if (state.isUploadingAndTranscribingAudio ||
        state.isSendingAnswer
    ) {
        ProcessingDialog()
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
                        text = formatDuration(state.totalSessionDuration.inWholeMilliseconds),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(visible = state.showQuestionCard) {
                    QuestionCard(
                        questionOrder = state.currentSession?.currentQuestion?.questionOrder,
                        questionText = state.currentSession?.currentQuestion?.questionText,
                        isReadingQuestion = state.isReadingQuestion,
                        onPlayClick = { onAction(PracticeSessionAction.ListenToAIReadingCurrentQuestion) },
                        onStopClick = { onAction(PracticeSessionAction.PauseListeningToCurrentQuestion) }
                    )
                }

                AnimatedVisibility(visible = state.isRecording) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        RecordingWave(
                            volumeBars = state.volumeBars,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                        )
                        RecordingDurationCard(
                            durationMs = state.recordingDuration.inWholeMilliseconds,
                            isRecording = state.isRecording
                        )
                    }
                }
                PracticeSessionBottomSection(
                    isRecording = state.isRecording,
                    recordedAudioPath = state.recordedAudioPath,
                    isPlayingAudio = state.isPlayingAudio,
                    playbackDurationMs = state.playbackDurationMs,
                    playbackPositionMs = state.playbackPositionMs,
                    showQuestionCard = state.showQuestionCard,
                    onAction = onAction
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            AmbientStageBackdrop()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CenterStage(
                            isReadingQuestion = state.isReadingQuestion,
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
            }
        }
    }
}
