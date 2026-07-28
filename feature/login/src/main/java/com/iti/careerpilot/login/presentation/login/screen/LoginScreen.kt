package com.iti.careerpilot.login.presentation.login.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.core.designsystem.components.LoadingDialog
import com.iti.careerpilot.login.R
import com.iti.careerpilot.login.presentation.login.LoginAction
import com.iti.careerpilot.login.presentation.login.LoginEvent
import com.iti.careerpilot.login.presentation.login.LoginState
import com.iti.careerpilot.login.presentation.login.LoginViewModel
import com.iti.careerpilot.login.presentation.login.screen.component.LoginGreeting
import com.iti.careerpilot.login.presentation.login.screen.component.LoginHeader
import com.iti.careerpilot.login.presentation.login.screen.component.PhoneInputField

@Composable
fun LoginRoot(
    openOTP: (phoneNumber: String) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvent(viewModel.events) { event ->
        when (event) {
            is LoginEvent.NavigateToOtp -> {
                openOTP(event.phoneNumber)
                viewModel.onAction(LoginAction.StopIsLoading)
            }
        }
    }

    LoginScreen(
        state = state,
        onAction = viewModel::onAction
    )
    if (state.isLoading) {
        LoadingDialog(
            title = stringResource(R.string.sending_otp)
        )
    }
}

@Composable
fun LoginScreen(
    state: LoginState,
    onAction: (LoginAction) -> Unit,
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .imePadding()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(vertical = 16.dp, horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    LoginHeader()
                }

                item {
                    LoginGreeting()
                }
                item {
                    Text(
                        text = stringResource(id = R.string.phone_number_label),
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.5.sp),
                        color = CareerPilotPalette.gray400
                    )
                    Spacer(Modifier.height(8.dp))
                    PhoneInputField(
                        phoneNumber = state.phoneNumber,
                        isLoading = state.isLoading,
                        onPhoneNumberChange = { onAction(LoginAction.PhoneNumberChanged(it)) },
                        onRegionChange = { onAction(LoginAction.RegionChanged(it)) }
                    )
                }
            }
            Box(
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp)
            ) {
                CareerPilotButton(
                    text = if (state.isInCooldown) {
                        stringResource(id = R.string.btn_retry_in_seconds, state.cooldownSecondsRemaining)
                    } else {
                        stringResource(id = R.string.btn_continue)
                    },
                    onClick = { onAction(LoginAction.SendOtpClicked) },
                    enabled = state.phoneNumber.isNotBlank() && !state.isInCooldown
                )
            }
        }

    }
}
