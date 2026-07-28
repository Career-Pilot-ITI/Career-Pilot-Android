package com.iti.careerpilot.login.presentation.otp.screen.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.login.R

@Composable
fun OtpResendRow(
    secondsRemaining: Int,
    canResend: Boolean,
    onResend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (secondsRemaining > 0) {
            Row {
                Text(
                    text = stringResource(id = R.string.otp_resend_in_prefix),
                    style = MaterialTheme.typography.labelMedium,
                    color = CareerPilotPalette.gray400,
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = formatCountdown(secondsRemaining),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        } else {
            Text(text = "", style = MaterialTheme.typography.labelMedium)
        }

        TextButton(
            onClick = onResend,
            enabled = canResend
        ) {
            Text(
                text = stringResource(id = R.string.otp_resend),
                style = MaterialTheme.typography.labelMedium,
                color = if (canResend) MaterialTheme.colorScheme.primary else CareerPilotPalette.gray400,
            )
        }
    }
}

private fun formatCountdown(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
