package com.iti.careerpilot.login.presentation.otp.screen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.AudioWaveform
import com.iti.careerpilot.core.designsystem.components.SuccessCheckmark
import com.iti.careerpilot.login.R

@Composable
fun OtpSuccessDialog() {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(Dimens.SpaceXXL),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            SuccessCheckmark()

            Text(
                modifier = Modifier.padding(top = Dimens.SpaceXXL),
                text = stringResource(id = R.string.otp_success_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Text(
                modifier = Modifier.padding(top = Dimens.SpaceS),
                text = stringResource(id = R.string.otp_success_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = CareerPilotPalette.gray400,
                textAlign = TextAlign.Center,
            )

            AudioWaveform(
                modifier = Modifier.padding(top = Dimens.SpaceXXXL),
            )
        }
    }
}
