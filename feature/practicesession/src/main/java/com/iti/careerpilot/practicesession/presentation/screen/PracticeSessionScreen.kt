package com.iti.careerpilot.practicesession.presentation.screen

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.common.PermissionsDialog
import com.iti.careerpilot.core.designsystem.components.LoadingDialog
import com.iti.careerpilot.practicesession.R
import com.iti.careerpilot.practicesession.presentation.action.PracticeSessionAction
import com.iti.careerpilot.practicesession.presentation.event.PracticeSessionEvent
import com.iti.careerpilot.practicesession.presentation.screen.components.CenterStage
import com.iti.careerpilot.practicesession.presentation.screen.components.ConfirmationDialog
import com.iti.careerpilot.practicesession.presentation.screen.components.PracticeSessionBottomSection
import com.iti.careerpilot.practicesession.presentation.screen.components.PracticeSessionSettingsBottomSheet
import com.iti.careerpilot.practicesession.presentation.screen.components.ProcessingDialog
import com.iti.careerpilot.practicesession.presentation.screen.components.QuestionCard
import com.iti.careerpilot.practicesession.presentation.screen.components.TopErrorNotification
import com.iti.careerpilot.practicesession.presentation.screen.util.formatDuration
import com.iti.careerpilot.practicesession.presentation.state.PracticeSessionState
import com.iti.careerpilot.practicesession.presentation.viewmodel.PracticeSessionViewModel
import com.iti.common.util.UIText
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun PracticeSessionRoot(
    trackId: Long,
    sessionId: Long? = null,
    onBack: () -> Unit,
    onNavigateToResult: (Long) -> Unit,
    viewModel: PracticeSessionViewModel = hiltViewModel()
) {
    var errorMessage by remember { mutableStateOf<UIText?>(null) }

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
    }

    if (state.showSettingsBottomSheet) {
        PracticeSessionSettingsBottomSheet(
            autoReadQuestion = state.autoReadQuestion,
            onAutoReadToggle = { viewModel.onAction(PracticeSessionAction.ToggleAutoReadQuestion(it)) },
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
            icon = Icons.AutoMirrored.Filled.Logout,
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
            icon = Icons.Default.DeleteForever,
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
    if (state.isLoadingSession) {
        LoadingDialog()
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
                }
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
                        onPlayClick = { onAction(PracticeSessionAction.ListenToAIReadingCurrentQuestion) },
                        onStopClick = { onAction(PracticeSessionAction.PauseListeningToCurrentQuestion) }
                    )
                }

                AnimatedVisibility(visible = state.isRecording) {
                    Card(
                        modifier = Modifier.padding(bottom = 8.dp),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = formatDuration(state.recordingDuration.inWholeMilliseconds),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(color = Color.Red, shape = CircleShape)
                            )
                        }
                    }
                }
                PracticeSessionBottomSection(
                    isRecording = state.isRecording,
                    recordedAudioPath = state.recordedAudioPath,
                    isPlayingAudio = state.isPlayingAudio,
                    playbackDurationMs = state.playbackDurationMs,
                    playbackPositionMs = state.playbackPositionMs,
                    amplitudes = state.amplitudes,
                    showQuestionCard = state.showQuestionCard,
                    onAction = onAction
                )
            }
        }
    ) { innerPadding ->
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
                        isRecording = state.isRecording,
                        amplitudes = state.amplitudes,
                        isReadingQuestion = state.isReadingQuestion,
                        hasRecordedAudio = state.recordedAudioPath != null,
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
