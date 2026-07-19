package com.iti.careerpilot.editprofile.presentation.screen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard

@Composable
fun LoadingDialog() {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        CareerPilotCard(
            useShadow = false,
            modifier = Modifier
        ) {
            Box(modifier = Modifier.padding(20.dp)) {
                CircularWavyProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}