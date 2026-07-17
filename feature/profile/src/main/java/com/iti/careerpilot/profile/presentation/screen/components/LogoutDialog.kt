package com.iti.careerpilot.profile.presentation.screen.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.iti.careerpilot.core.designsystem.CareerPilotPalette


@Composable
fun LogoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Log out?")
                },
        text = { Text("You'll need to sign in again to access your account.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Log out", color = CareerPilotPalette.coral)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}