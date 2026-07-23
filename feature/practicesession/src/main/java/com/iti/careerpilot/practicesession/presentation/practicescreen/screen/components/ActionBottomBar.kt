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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.practicesession.R


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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PracticeIconButton(
            onClick = onToggleQuestionCard,
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(
                    if (showQuestionCard) R.drawable.ic_expand_down
                    else R.drawable.ic_expand_up
                ),
                contentDescription = stringResource(R.string.toggle_question_card),
                modifier = Modifier.size(28.dp)
            )
        }

        Box(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.background,
                    CircleShape
                )
                .drawBehind {
                    val maxRadius = size.width / 2f
                    val color = extendedColors.pulsingRing
                    drawCircle(
                        color = color.copy(alpha = 0.2f),
                        radius = maxRadius,
                        center = center
                    )
                    drawCircle(
                        color = color.copy(alpha = 0.2f),
                        radius = maxRadius - 30,
                        center = center
                    )
                    drawCircle(
                        color = color.copy(alpha = 0.2f),
                        radius = maxRadius - 60,
                        center = center
                    )
                    drawRect(
                        brush = Brush
                            .radialGradient(
                                0f to extendedColors.radialGradientStart,
                                1f to extendedColors.radialGradientEnd,
                                radius = 200f,
                                center = Offset(150f, 150f)
                            )
                    )
                }
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
                size = 80.dp,
            )
        }

        PracticeIconButton(onClick = onOpenSettings) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_more),
                contentDescription = stringResource(R.string.session_settings),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
