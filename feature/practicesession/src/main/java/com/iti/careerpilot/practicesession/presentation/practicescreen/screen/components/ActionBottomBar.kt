package com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.practicesession.R


import androidx.compose.material3.Surface

@Composable
fun ActionBottomBar(
    isRecording: Boolean,
    showQuestionCard: Boolean,
    onOpenSettings: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onShowPermissionDialog: () -> Unit,
    onToggleQuestionCard: () -> Unit,
) {
    val context = LocalContext.current
    val extendedColors = CareerPilotTheme.extendedColors

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.90f),
        shape = CircleShape,
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PracticeIconButton(
                onClick = onToggleQuestionCard,
                iconId = if (showQuestionCard) R.drawable.ic_expand_down
                else R.drawable.ic_expand_up,
                descriptionId = R.string.toggle_question_card
            )

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background, CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            1f to MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        ),
                        shape = CircleShape
                    )
                    .border(1.dp, extendedColors.actionBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                LargeGradientIconButton(
                    icon = ImageVector.vectorResource(
                        if (isRecording) R.drawable.ic_stop
                        else R.drawable.ic_mic
                    ),
                    contentDescription = if (isRecording) {
                        stringResource(R.string.stop_recording)
                    } else {
                        stringResource(R.string.start_recording)
                    },
                    onClick = {
                        if (isRecording) {
                            onStopRecording()
                        } else {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) onStartRecording()
                            else onShowPermissionDialog()
                        }
                    },
                    size = 72.dp,
                )
            }

            PracticeIconButton(
                onClick = onOpenSettings,
                iconId = R.drawable.ic_more,
                descriptionId = R.string.session_settings
            )
        }
    }
}
