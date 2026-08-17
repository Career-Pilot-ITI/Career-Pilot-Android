package com.iti.careerpilot.core.designsystem.components

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.R
import com.iti.careerpilot.core.designsystem.common.GradientIcon
import com.iti.common.util.ChallengeShareHelper
import com.iti.common.util.QrCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val scope = rememberCoroutineScope()
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var copied by remember { mutableStateOf(false) }

    val qrForegroundColor = CareerPilotPalette.navyDark.toArgb()
    val qrBackgroundColor = android.graphics.Color.WHITE

    LaunchedEffect(shareUrl) {
        qrBitmap = withContext(Dispatchers.Default) {
            QrCodeGenerator.generateQrBitmap(
                content = shareUrl,
                sizePx = 512,
                foregroundColor = qrForegroundColor,
                backgroundColor = qrBackgroundColor
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        OutlinedCard(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Gradient Icon
                GradientIcon(
                    icon = Icons.Default.Share,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(48.dp)
                )

                // Title & Subtitle
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                // QR Code Container Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.size(190.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        qrBitmap?.let { bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = stringResource(R.string.share_challenge_scan_qr),
                                modifier = Modifier.fillMaxSize()
                            )
                        } ?: run {
                            LoadingWave(
                                color = MaterialTheme.colorScheme.primary,
                                barCount = 12
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.share_challenge_scan_qr),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )

                // Interactive Link Card with Copy Action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            shape = MaterialTheme.shapes.medium
                        )
                        .clickable {
                            clipboardManager.setText(AnnotatedString(shareUrl))
                            copied = true
                            scope.launch {
                                delay(2000)
                                copied = false
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = shareUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = if (copied) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (copied) {
                                stringResource(R.string.share_challenge_copied)
                            } else {
                                stringResource(R.string.share_challenge_copy_link)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (copied) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Primary Action Button (Shares QR Code Image + Message by default)
                CareerPilotButton(
                    text = stringResource(R.string.share_challenge_button),
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            val sendIntent = ChallengeShareHelper.createShareChallengeIntent(
                                context = context,
                                message = shareMessage,
                                qrBitmap = qrBitmap
                            )
                            val chooser = Intent.createChooser(sendIntent, title)
                            withContext(Dispatchers.Main) {
                                context.startActivity(chooser)
                            }
                        }
                    },
                    variant = ButtonVariant.PRIMARY,
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                CareerPilotButton(
                    text = stringResource(R.string.share_challenge_close),
                    onClick = onDismiss,
                    variant = ButtonVariant.GHOST,
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
            subtitle = "Android Development (Senior)",
            shareUrl = "https://career-pilot-indol.vercel.app/challenge?id=ch_123&code=CODE456",
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
            subtitle = "Android Development (Senior)",
            shareUrl = "https://career-pilot-indol.vercel.app/challenge?id=ch_123&code=CODE456",
            shareMessage = "Join my mock interview challenge on Career Pilot!",
            onDismiss = {}
        )
    }
}
