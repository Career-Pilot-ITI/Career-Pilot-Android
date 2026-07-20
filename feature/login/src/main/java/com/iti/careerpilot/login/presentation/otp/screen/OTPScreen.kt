package com.iti.careerpilot.login.presentation.otp.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.login.R
import com.iti.careerpilot.login.presentation.otp.OTPAction
import com.iti.careerpilot.login.presentation.otp.OTPEvent
import com.iti.careerpilot.login.presentation.otp.OTPState
import com.iti.careerpilot.login.presentation.otp.OTPViewModel
import com.iti.careerpilot.login.presentation.otp.screen.component.OtpCodeInput
import com.iti.careerpilot.login.presentation.otp.screen.component.OtpResendRow
import com.iti.careerpilot.login.presentation.otp.screen.component.OtpSuccessDialog

@Composable
fun OTPRoot(
    phoneNumber: String,
    openHome: () -> Unit,
    openOnboarding: () -> Unit,
    onBack: () -> Unit,
    viewModel: OTPViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(phoneNumber) {
        viewModel.onAction(OTPAction.PhoneNumberReceived(phoneNumber))
    }

    ObserveEvent(viewModel.events) { event ->
        when (event) {
            is OTPEvent.NavigateToHome -> openHome()
            is OTPEvent.NavigateToOnBoarding -> openOnboarding()
        }
    }

    OTPScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack = onBack,
    )

    if (state.isVerified) {
        OtpSuccessDialog()
    }
}

@Composable
fun OTPScreen(
    state: OTPState,
    onAction: (OTPAction) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .padding(Dimens.SpaceXXL),
    ) {
        BackButton(onBack = onBack)

        Spacer(modifier = Modifier.height(Dimens.SpaceXXXL))

        Text(
            text = stringResource(id = R.string.otp_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceS))

        Text(
            text = stringResource(id = R.string.otp_sent_to, state.phoneNumber),
            style = MaterialTheme.typography.bodyMedium,
            color = CareerPilotPalette.gray400,
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceXXL))

        OtpCodeInput(
            code = state.code,
            length = OTPViewModel.OTP_LENGTH,
            enabled = !state.isLoading && !state.isVerified,
            onCodeChange = { onAction(OTPAction.CodeChanged(it)) },
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceL))

        OtpResendRow(
            secondsRemaining = state.resendSecondsRemaining,
            canResend = state.canResend,
            onResend = { onAction(OTPAction.ResendClicked) },
        )

        state.error?.let { error ->
            Spacer(modifier = Modifier.height(Dimens.SpaceL))
            Text(
                text = error.asString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun BackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.clickable(onClick = onBack),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
            contentDescription = stringResource(id = R.string.otp_back),
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(Dimens.SpaceXXL),
        )
        Text(
            text = stringResource(id = R.string.otp_back),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
