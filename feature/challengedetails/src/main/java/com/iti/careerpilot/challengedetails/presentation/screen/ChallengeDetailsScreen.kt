package com.iti.careerpilot.challengedetails.presentation.screen

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.challengedetails.R
import com.iti.careerpilot.challengedetails.presentation.action.ChallengeDetailsAction
import com.iti.careerpilot.challengedetails.presentation.event.ChallengeDetailsEvent
import com.iti.careerpilot.challengedetails.presentation.state.ChallengeDetailsState
import com.iti.careerpilot.challengedetails.presentation.viewmodel.ChallengeDetailsViewModel
import com.iti.careerpilot.challengefirestore.ChallengeType
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.common.PermissionsDialog
import com.iti.careerpilot.core.designsystem.components.BackIconButton
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.core.designsystem.components.LoadingWave


@Composable
fun ChallengeDetailsScreenRoot(
    challengeId: String,
    onBack: () -> Unit,
    onNavigateToPractice: (trackId: Long, challengeId: String, isVideo: Boolean, posture: Boolean, hands: Boolean) -> Unit,
    viewModel: ChallengeDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(challengeId) {
        viewModel.onAction(ChallengeDetailsAction.Initial(challengeId))
    }

    LaunchedEffect(Unit) {
        val micGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED

        val cameraGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED

        if (micGranted) {
            viewModel.onAction(ChallengeDetailsAction.MicrophonePermissionChanged(isGranted = true))
        }
        if (cameraGranted) {
            viewModel.onAction(ChallengeDetailsAction.CameraPermissionChanged(isGranted = true))
        }
    }

    ObserveEvent(viewModel.events) { event ->
        when (event) {
            ChallengeDetailsEvent.NavigateBack -> onBack()
            is ChallengeDetailsEvent.NavigateToPractice -> onNavigateToPractice(
                event.challenge.trackId,
                event.challenge.id,
                event.challenge.type == ChallengeType.VIDEO_AND_AUDIO,
                event.challenge.videoAnalysisConfig?.analyzePosture ?: false,
                event.challenge.videoAnalysisConfig?.analyzeHands ?: false
            )
        }
    }

    if (state.showMicPermissionDialog) {
        PermissionsDialog(
            title = stringResource(R.string.challenge_details_mic_permission_title),
            text = stringResource(R.string.challenge_details_mic_permission_text),
            icon = Icons.Filled.Mic,
            cancel = stringResource(R.string.challenge_details_cancel),
            allow = stringResource(R.string.challenge_details_allow),
            onDismiss = { viewModel.onAction(ChallengeDetailsAction.PermissionDialogDismissed) },
            onGranted = {
                viewModel.onAction(
                    ChallengeDetailsAction.MicrophonePermissionChanged(isGranted = true)
                )
            },
            neededPermissions = arrayOf(Manifest.permission.RECORD_AUDIO),
        )
    }

    if (state.showCameraPermissionDialog) {
        PermissionsDialog(
            title = stringResource(R.string.challenge_details_camera_permission_title),
            text = stringResource(R.string.challenge_details_camera_permission_text),
            icon = Icons.Filled.Videocam,
            cancel = stringResource(R.string.challenge_details_cancel),
            allow = stringResource(R.string.challenge_details_allow),
            onDismiss = { viewModel.onAction(ChallengeDetailsAction.CameraPermissionDialogDismissed) },
            onGranted = {
                viewModel.onAction(
                    ChallengeDetailsAction.CameraPermissionChanged(isGranted = true)
                )
            },
            neededPermissions = arrayOf(Manifest.permission.CAMERA),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.challenge_details_title)) },
                navigationIcon = {
                    BackIconButton(onBack = { viewModel.onAction(ChallengeDetailsAction.OnBackClicked) })
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        ChallengeDetailsScreenContent(
            modifier = Modifier.padding(padding), state = state, onAction = viewModel::onAction
        )
    }
}

@Composable
fun ChallengeDetailsScreenContent(
    modifier: Modifier = Modifier,
    state: ChallengeDetailsState,
    onAction: (ChallengeDetailsAction) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                LoadingWave(color = MaterialTheme.colorScheme.primary)
            }
        } else if (state.challenge != null) {
            val challenge = state.challenge

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = challenge.trackName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
                Text(
                    text = stringResource(R.string.challenge_details_by, challenge.creatorName),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(
                        R.string.challenge_details_seniority, challenge.seniorityLevel.name
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            CareerPilotCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        stringResource(R.string.challenge_details_configuration),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    DetailRow(
                        label = stringResource(R.string.challenge_details_type),
                        value = challenge.type.name.replace("_", " ")
                    )
                    DetailRow(
                        label = stringResource(R.string.challenge_details_questions_label),
                        value = stringResource(
                            R.string.challenge_details_questions_count, challenge.questions.size
                        )
                    )
                    DetailRow(
                        label = stringResource(R.string.challenge_details_estimated_time),
                        value = stringResource(
                            R.string.challenge_details_minutes, challenge.questions.size * 2
                        )
                    )

                    if (challenge.type == ChallengeType.VIDEO_AND_AUDIO && challenge.videoAnalysisConfig != null) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                        Text(
                            stringResource(R.string.challenge_details_video_analysis),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        DetailRow(
                            label = stringResource(R.string.challenge_details_face_analysis),
                            value = stringResource(R.string.challenge_details_enabled),
                            color = MaterialTheme.colorScheme.primary
                        )
                        DetailRow(
                            label = stringResource(R.string.challenge_details_posture_analysis),
                            value = if (challenge.videoAnalysisConfig?.analyzePosture == true) stringResource(
                                R.string.challenge_details_enabled
                            ) else stringResource(R.string.challenge_details_disabled)
                        )
                        DetailRow(
                            label = stringResource(R.string.challenge_details_hands_analysis),
                            value = if (challenge.videoAnalysisConfig?.analyzeHands == true) stringResource(
                                R.string.challenge_details_enabled
                            ) else stringResource(R.string.challenge_details_disabled)
                        )
                    }
                }
            }

            TipsCard()

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PermissionRow(
                    label = stringResource(R.string.challenge_details_mic_access),
                    isGranted = state.isMicrophoneGranted,
                    onClick = { onAction(ChallengeDetailsAction.MicrophoneRowClicked) }
                )

                if (challenge.type == ChallengeType.VIDEO_AND_AUDIO) {
                    PermissionRow(
                        label = stringResource(R.string.challenge_details_camera_access),
                        isGranted = state.isCameraGranted,
                        onClick = { onAction(ChallengeDetailsAction.CameraRowClicked) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            CareerPilotButton(
                text = stringResource(R.string.challenge_details_begin),
                onClick = { onAction(ChallengeDetailsAction.BeginChallengeClicked) },
                enabled = state.canBegin,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun PermissionRow(label: String, isGranted: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (isGranted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = 0.5f
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            if (isGranted) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    stringResource(R.string.challenge_details_tap_to_grant),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun TipsCard() {
    CareerPilotCard(
        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(R.string.challenge_details_prep_tips),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            BulletPoint(stringResource(R.string.challenge_details_tip_quiet))
            BulletPoint(stringResource(R.string.challenge_details_tip_internet))
            BulletPoint(stringResource(R.string.challenge_details_tip_focus))
            BulletPoint(stringResource(R.string.challenge_details_tip_lighting))
        }
    }
}

@Composable
fun BulletPoint(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("•", style = MaterialTheme.typography.bodySmall)
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}
