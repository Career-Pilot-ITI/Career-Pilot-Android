package com.iti.careerpilot.core.designsystem.components

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.R
import com.iti.common.util.ChallengeShareHelper
import com.iti.common.util.QrCodeGenerator

@Composable
fun ShareChallengeDialog(
    title: String,
    subtitle: String,
    shareUrl: String,
    shareMessage: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var copied by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val bgColor = MaterialTheme.colorScheme.surface.toArgb()

    LaunchedEffect(shareUrl, primaryColor, bgColor) {
        qrBitmap = QrCodeGenerator.generateQrBitmap(
            content = shareUrl,
            sizePx = 512,
            foregroundColor = primaryColor,
            backgroundColor = bgColor
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // QR Code Display Box
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    qrBitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = stringResource(R.string.share_challenge_scan_qr),
                            modifier = Modifier.size(176.dp)
                        )
                    } ?: run {
                        LoadingWave(
                            color = MaterialTheme.colorScheme.primary,
                            barCount = 12
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.share_challenge_scan_qr),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                // Copy Link Button
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(shareUrl))
                        copied = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (copied) {
                            stringResource(R.string.share_challenge_copied)
                        } else {
                            stringResource(R.string.share_challenge_copy_link)
                        }
                    )
                }

                // Share Challenge System Intent Button
                CareerPilotButton(
                    text = stringResource(R.string.share_challenge_button),
                    onClick = {
                        val sendIntent = ChallengeShareHelper.createShareTextIntent(shareMessage)
                        val chooser = Intent.createChooser(sendIntent, title)
                        context.startActivity(chooser)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Share QR Image Button
                qrBitmap?.let { bitmap ->
                    OutlinedButton(
                        onClick = {
                            val file = ChallengeShareHelper.saveQrBitmapToCache(context, bitmap)
                            if (file != null) {
                                val sendIntent = ChallengeShareHelper.createShareImageIntent(
                                    context = context,
                                    imageFile = file,
                                    caption = shareMessage
                                )
                                val chooser = Intent.createChooser(sendIntent, title)
                                context.startActivity(chooser)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.share_challenge_share_qr_image))
                    }
                }

                CareerPilotButton(
                    text = stringResource(R.string.share_challenge_close),
                    onClick = onDismiss,
                    variant = ButtonVariant.SECONDARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun ShareChallengeDialogPreviewLight() {
    CareerPilotTheme(darkTheme = false) {
        ShareChallengeDialog(
            title = "Share Challenge",
            subtitle = "Invite peers to practice together",
            shareUrl = "https://career-pilot-indol.vercel.app/challenge?id=123",
            shareMessage = "Join my mock interview challenge on Career Pilot!",
            onDismiss = {}
        )
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF0E1428)
@Composable
private fun ShareChallengeDialogPreviewDark() {
    CareerPilotTheme(darkTheme = true) {
        ShareChallengeDialog(
            title = "Share Challenge",
            subtitle = "Invite peers to practice together",
            shareUrl = "https://career-pilot-indol.vercel.app/challenge?id=123",
            shareMessage = "Join my mock interview challenge on Career Pilot!",
            onDismiss = {}
        )
    }
}
