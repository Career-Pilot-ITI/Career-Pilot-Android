package com.iti.careerpilot.home.presentation.ready.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.common.PermissionsDialog
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.core.designsystem.components.CoinTopUpBottomSheet
import com.iti.careerpilot.core.designsystem.components.FeatureGateBottomSheet
import com.iti.careerpilot.core.designsystem.components.FeaturePricingBadge
import com.iti.careerpilot.home.R
import com.iti.careerpilot.home.presentation.components.CareerPilotTopBar
import com.iti.careerpilot.home.presentation.ready.ReadyToPracticeEffect
import com.iti.careerpilot.home.presentation.ready.ReadyToPracticeIntent
import com.iti.careerpilot.home.presentation.ready.ReadyToPracticeState
import com.iti.careerpilot.home.presentation.ready.ReadyToPracticeViewModel
import com.iti.careerpilot.home.presentation.ready.screen.components.CameraRow
import com.iti.careerpilot.home.presentation.ready.screen.components.LandmarkFeatureChips
import com.iti.careerpilot.home.presentation.ready.screen.components.MicrophoneRow
import com.iti.careerpilot.home.presentation.ready.screen.components.TipsCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadyToPracticeRoot(
    trackId: Long,
    trackName: String,
    workspaceId: Long? = null,
    onBack: () -> Unit,
    openPractice: (trackId: Long, workspaceId: Long?, isVideo: Boolean, enablePosture: Boolean, enableHands: Boolean) -> Unit,
    openPaywall: () -> Unit = {},
    viewModel: ReadyToPracticeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(trackId, trackName, workspaceId) {
        viewModel.onIntent(ReadyToPracticeIntent.Initial(trackId, trackName, workspaceId))
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
        
        val cameraGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            viewModel.onIntent(ReadyToPracticeIntent.MicrophonePermissionChanged(isGranted = true))
        }
        if (cameraGranted) {
            viewModel.onIntent(ReadyToPracticeIntent.CameraPermissionChanged(isGranted = true))
        }
    }

    ObserveEvent(viewModel.events) { event ->
        when (event) {
            is ReadyToPracticeEffect.NavigateToPractice -> openPractice(
                event.trackId,
                event.workspaceId,
                event.isVideo,
                event.enablePosture,
                event.enableHands,
            )
            ReadyToPracticeEffect.NavigateToPaywall -> openPaywall()
            ReadyToPracticeEffect.NavigateBack -> onBack()
        }
    }

    if (state.isPermissionDialogVisible) {
        PermissionsDialog(
            title = stringResource(R.string.ready_permission_title),
            text = stringResource(R.string.ready_permission_text),
            icon = Icons.Filled.Mic,
            cancel = stringResource(R.string.ready_permission_cancel),
            allow = stringResource(R.string.ready_permission_allow),
            onDismiss = { viewModel.onIntent(ReadyToPracticeIntent.PermissionDialogDismissed) },
            onGranted = {
                viewModel.onIntent(
                    ReadyToPracticeIntent.MicrophonePermissionChanged(isGranted = true)
                )
            },
            neededPermissions = arrayOf(Manifest.permission.RECORD_AUDIO),
        )
    }

    if (state.showCameraPermissionDialog) {
        PermissionsDialog(
            title = stringResource(R.string.ready_camera_permission_title),
            text = stringResource(R.string.ready_camera_permission_text),
            icon = Icons.Filled.Videocam,
            cancel = stringResource(R.string.ready_permission_cancel),
            allow = stringResource(R.string.ready_permission_allow),
            onDismiss = { viewModel.onIntent(ReadyToPracticeIntent.CameraPermissionDialogDismissed) },
            onGranted = {
                viewModel.onIntent(
                    ReadyToPracticeIntent.CameraPermissionChanged(isGranted = true)
                )
            },
            neededPermissions = arrayOf(Manifest.permission.CAMERA),
        )
    }

    if (state.showVideoGateSheet) {
        FeatureGateBottomSheet(
            featureName = stringResource(R.string.video_session),
            requiredPlan = state.videoGateRequiredPlan,
            planFeatures = state.videoGatePlanFeatures,
            onUpgradeClick = { viewModel.onIntent(ReadyToPracticeIntent.UpgradeFromVideoGate) },
            onDismiss = { viewModel.onIntent(ReadyToPracticeIntent.DismissVideoGateSheet) },
        )
    }

    if (state.showCoinTopUpSheet) {
        CoinTopUpBottomSheet(
            coinCost = state.coinTopUpRequiredCost,
            currentBalance = state.coinBalance,
            onBuyCoins = { viewModel.onIntent(ReadyToPracticeIntent.BuyCoinsClicked) },
            onDismiss = { viewModel.onIntent(ReadyToPracticeIntent.DismissCoinTopUpSheet) },
        )
    }

    ReadyToPracticeScreen(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun ReadyToPracticeScreen(
    state: ReadyToPracticeState,
    onIntent: (ReadyToPracticeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CareerPilotTopBar(
                onBack = { onIntent(ReadyToPracticeIntent.CancelClicked) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(start = Dimens.SpaceXXL, end = Dimens.SpaceXXL, bottom = Dimens.SpaceXXL),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
        ) {
            if (state.trackName.isNotBlank()) {
                Text(
                    text = state.trackName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceS),
                )
            }

            Text(
                text = stringResource(R.string.ready_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.ready_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // High-Contrast Interview Mode Selection (Audio vs Video)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
            ) {
                // Audio Mode Card
                val isAudioSelected = !state.isVideoMode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isAudioSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                        .border(
                            width = if (isAudioSelected) 2.dp else 1.dp,
                            color = if (isAudioSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onIntent(ReadyToPracticeIntent.SelectAudioMode) }
                        .padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceL),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = if (isAudioSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = stringResource(R.string.audio_session),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (isAudioSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isAudioSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        )
                        FeaturePricingBadge(
                            coinCost = state.voiceCoinCost,
                            compact = true,
                        )
                    }
                }

                // Video Mode Card
                val isVideoSelected = state.isVideoMode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isVideoSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                        .border(
                            width = if (isVideoSelected) 2.dp else 1.dp,
                            color = if (isVideoSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onIntent(ReadyToPracticeIntent.SelectVideoMode) }
                        .padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceL),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = if (isVideoSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                modifier = Modifier.size(26.dp)
                            )
                            if (!state.isMaxPlan) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = stringResource(R.string.pro_badge),
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.video_session),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (isVideoSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isVideoSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                            )
                            if (!state.isMaxPlan) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.pro_badge),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        FeaturePricingBadge(
                            coinCost = state.videoCoinCost,
                            compact = true,
                        )
                    }
                }
            }

            if (state.isVideoMode) {
                LandmarkFeatureChips(
                    enablePosture = state.enablePostureTracking,
                    enableHands = state.enableHandTracking,
                    onPostureToggle = { onIntent(ReadyToPracticeIntent.TogglePostureTracking(it)) },
                    onHandsToggle = { onIntent(ReadyToPracticeIntent.ToggleHandTracking(it)) },
                )
            }

            TipsCard()

            MicrophoneRow(
                isGranted = state.isMicrophoneGranted,
                onClick = { onIntent(ReadyToPracticeIntent.MicrophoneRowClicked) },
            )

            if (state.isVideoMode) {
                CameraRow(
                    isGranted = state.isCameraGranted,
                    onClick = { onIntent(ReadyToPracticeIntent.CameraRowClicked) },
                )
            }

            FeaturePricingBadge(
                access = if (state.isVideoMode) state.videoInterviewAccess else state.audioInterviewAccess,
                coinBalance = state.coinBalance,
                planDisplayName = state.planDisplayName,
                coinCost = if (state.isVideoMode) state.videoCoinCost else state.voiceCoinCost,
                onUpgradeClick = { onIntent(ReadyToPracticeIntent.UpgradeFromVideoGate) },
            )

            Spacer(modifier = Modifier.height(Dimens.SpaceM))

            CareerPilotButton(
                text = stringResource(R.string.ready_begin),
                onClick = { onIntent(ReadyToPracticeIntent.BeginInterviewClicked) },
                enabled = state.canBegin,
            )
        }
    }
}
