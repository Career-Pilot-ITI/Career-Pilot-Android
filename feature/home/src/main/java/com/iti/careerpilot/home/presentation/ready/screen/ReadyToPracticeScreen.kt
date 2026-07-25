package com.iti.careerpilot.home.presentation.ready.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.CareerPilotShapes
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.common.PermissionsDialog
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.home.R
import com.iti.careerpilot.home.presentation.components.CareerPilotTopBar
import com.iti.careerpilot.home.presentation.ready.ReadyToPracticeAction
import com.iti.careerpilot.home.presentation.ready.ReadyToPracticeEvent
import com.iti.careerpilot.home.presentation.ready.ReadyToPracticeState
import com.iti.careerpilot.home.presentation.ready.ReadyToPracticeViewModel

@Composable
fun ReadyToPracticeRoot(
    trackId: Long,
    trackName: String,
    onBack: () -> Unit,
    openInterview: (sessionId: Long) -> Unit,
    viewModel: ReadyToPracticeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(trackId, trackName) {
        viewModel.initialise(trackId = trackId, trackName = trackName)
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            viewModel.onAction(ReadyToPracticeAction.MicrophonePermissionChanged(isGranted = true))
        }
    }

    ObserveEvent(viewModel.events) { event ->
        when (event) {
            is ReadyToPracticeEvent.NavigateToInterview -> openInterview(event.sessionId)
            ReadyToPracticeEvent.NavigateBack -> onBack()
        }
    }

    if (state.isPermissionDialogVisible) {
        PermissionsDialog(
            title = stringResource(R.string.ready_permission_title),
            text = stringResource(R.string.ready_permission_text),
            icon = Icons.Filled.Mic,
            cancel = stringResource(R.string.ready_permission_cancel),
            allow = stringResource(R.string.ready_permission_allow),
            onDismiss = { viewModel.onAction(ReadyToPracticeAction.PermissionDialogDismissed) },
            onGranted = {
                viewModel.onAction(
                    ReadyToPracticeAction.MicrophonePermissionChanged(isGranted = true)
                )
            },
            neededPermissions = arrayOf(Manifest.permission.RECORD_AUDIO),
        )
    }

    ReadyToPracticeScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun ReadyToPracticeScreen(
    state: ReadyToPracticeState,
    onAction: (ReadyToPracticeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CareerPilotTopBar(
                onBack = { onAction(ReadyToPracticeAction.CancelClicked) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(start = Dimens.SpaceXXL, end = Dimens.SpaceXXL, bottom = Dimens.SpaceXXL),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
        ) {
            if (state.trackName.isNotBlank()) {
                Text(
                    text = state.trackName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = CareerPilotPalette.gray600,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(CareerPilotPalette.gray100)
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
                color = CareerPilotPalette.gray600,
            )

            TipsCard()

            MicrophoneRow(
                isGranted = state.isMicrophoneGranted,
                onClick = { onAction(ReadyToPracticeAction.MicrophoneRowClicked) },
            )

            Box(modifier = Modifier.weight(1f))

            CareerPilotButton(
                text = stringResource(R.string.ready_begin),
                onClick = { onAction(ReadyToPracticeAction.BeginInterviewClicked) },
                enabled = state.canBegin,
            )
        }
    }
}

@Composable
private fun TipsCard(modifier: Modifier = Modifier) {
    val tips = listOf(
        R.string.ready_tip_quiet,
        R.string.ready_tip_speak,
        R.string.ready_tip_think,
        R.string.ready_tip_wait,
    )

    CareerPilotCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Dimens.CardPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
        ) {
            tips.forEachIndexed { index, tipRes ->
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CareerPilotShapes.small)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = (index + 1).toString(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text = stringResource(tipRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun MicrophoneRow(
    isGranted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CareerPilotCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clickable(enabled = !isGranted, onClick = onClick)
                .padding(Dimens.CardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CareerPilotShapes.small)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.ready_mic_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(
                        if (isGranted) R.string.ready_mic_granted else R.string.ready_mic_required
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isGranted) CareerPilotPalette.green
                    else MaterialTheme.colorScheme.error,
                )
            }

            if (isGranted) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = CareerPilotPalette.green,
                )
            }
        }
    }
}

