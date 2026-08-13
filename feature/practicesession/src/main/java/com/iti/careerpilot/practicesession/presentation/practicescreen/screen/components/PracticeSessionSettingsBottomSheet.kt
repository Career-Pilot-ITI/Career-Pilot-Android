package com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.practicesession.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeSessionSettingsBottomSheet(
    autoReadQuestion: Boolean,
    showCameraPreviewToggle: Boolean,
    isCameraPreviewVisible: Boolean,
    enablePostureTracking: Boolean,
    enableHandTracking: Boolean,
    onAutoReadToggle: (Boolean) -> Unit,
    onCameraPreviewToggle: (Boolean) -> Unit,
    onPostureTrackingToggle: (Boolean) -> Unit,
    onHandTrackingToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(R.string.session_settings),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingsSwitchRow(
                label = stringResource(R.string.auto_read_question),
                description = stringResource(R.string.automatically_speak_the_question_when_it_s_shown),
                checked = autoReadQuestion,
                onCheckedChange = onAutoReadToggle
            )

            if (showCameraPreviewToggle) {
                SettingsSwitchRow(
                    label = stringResource(R.string.camera_preview),
                    description = stringResource(R.string.show_camera_during_session),
                    checked = isCameraPreviewVisible,
                    onCheckedChange = onCameraPreviewToggle,
                )
                SettingsSwitchRow(
                    label = stringResource(R.string.track_posture_and_body),
                    description = stringResource(R.string.analyze_body_language_and_posture),
                    checked = enablePostureTracking,
                    onCheckedChange = onPostureTrackingToggle,
                )
                SettingsSwitchRow(
                    label = stringResource(R.string.track_hand_gestures),
                    description = stringResource(R.string.analyze_hand_movements),
                    checked = enableHandTracking,
                    onCheckedChange = onHandTrackingToggle,
                )
            }
        }
    }
}
