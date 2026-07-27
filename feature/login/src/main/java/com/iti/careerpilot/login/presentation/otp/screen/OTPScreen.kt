package com.iti.careerpilot.login.presentation.otp.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.components.BackIconButton
import com.iti.careerpilot.core.designsystem.components.LoadingDialog
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

    if (state.isVerifyingOtp || state.isResendingOtp) {
        LoadingDialog(
            title = stringResource(
                id = if (state.isResendingOtp) R.string.sending_otp
                else R.string.verifying_otp
            ),
        )
    }
}

@Composable
fun OTPScreen(
    state: OTPState,
    onAction: (OTPAction) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    BackIconButton(onBack = onBack)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
        ) {
            item {
                Text(
                    text = stringResource(id = R.string.otp_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            item {
                Text(
                    text = stringResource(id = R.string.otp_sent_to, state.phoneNumber),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CareerPilotPalette.gray400,
                )
            }
            item {
                OtpCodeInput(
                    code = state.code,
                    length = OTPViewModel.OTP_LENGTH,
                    enabled = !state.isVerifyingOtp && !state.isVerified,
                    onCodeChange = { onAction(OTPAction.CodeChanged(it)) },
                )
            }
            item {
                OtpResendRow(
                    secondsRemaining = state.resendSecondsRemaining,
                    canResend = state.canResend,
                    onResend = { onAction(OTPAction.ResendClicked) },
                )
            }
        }
    }
}
