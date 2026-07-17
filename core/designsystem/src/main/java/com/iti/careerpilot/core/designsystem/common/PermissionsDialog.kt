package com.iti.careerpilot.core.designsystem.common

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun PermissionsDialog(
    title: String,
    text: String,
    icon: ImageVector,
    cancel: String,
    allow: String,
    onDismiss: () -> Unit,
    onGranted: () -> Unit,
    neededPermissions: Array<String>,
    allPermissionsNeeded: Boolean = true
) {

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        when  {
            (allPermissionsNeeded && permissionsResult.all { it.value }) ||
            (!allPermissionsNeeded && permissionsResult.any { it.value }) -> {
                onGranted()
                onDismiss()
            }
            else -> {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
                onDismiss()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        OutlinedCard {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .padding(16.dp)
            ) {
                GradientIcon(
                    icon = icon,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(48.dp)
                )
                Text(
                    text = title,
                    modifier = Modifier,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = text,
                    modifier = Modifier,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = cancel,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = {
                            permissionLauncher.launch(neededPermissions)
                        },
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = allow,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}