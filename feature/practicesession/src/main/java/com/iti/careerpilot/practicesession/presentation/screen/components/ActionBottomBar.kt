package com.iti.careerpilot.practicesession.presentation.screen.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.iti.careerpilot.practicesession.R


@Composable
fun ActionBottomBar(
    isRecording: Boolean,
    amplitudes: List<Float>,
    showQuestionCard: Boolean,
    onOpenSettings: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onShowPermissionDialog: () -> Unit,
    onToggleQuestionCard: () -> Unit,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onToggleQuestionCard,
            ) {
                Icon(
                    imageVector = if (showQuestionCard) Icons.Default.Description else Icons.Default.GraphicEq,
                    contentDescription = stringResource(R.string.toggle_question_card),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Box(
            modifier = Modifier.weight(2f),
            contentAlignment = Alignment.Center
        ) {
            AmplitudeRings(
                amplitudes = amplitudes,
                isRecording = isRecording,
            )
            LargeGradientIconButton(
                icon = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
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

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.session_settings),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}