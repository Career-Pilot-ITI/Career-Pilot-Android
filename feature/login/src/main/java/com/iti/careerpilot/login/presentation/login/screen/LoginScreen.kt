package com.iti.careerpilot.login.presentation.login.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.components.LoadingWave
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
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
            is LoginEvent.NavigateToOtp -> openOTP(event.phoneNumber)
        }
    }

    LoginScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun LoginScreen(
    state: LoginState,
    onAction: (LoginAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(Dimens.SpaceXXL)
            .fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(Dimens.SpaceXXXL + Dimens.SpaceXXXL))

        LoginHeader()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = Dimens.SpaceXXXXL)
        ) {
            LoginGreeting()

            Spacer(modifier = Modifier.height(Dimens.SpaceXXXL + Dimens.SpaceXS))

            Text(
                text = stringResource(id = R.string.phone_number_label),
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.5.sp),
                color = CareerPilotPalette.gray400
            )

            Spacer(modifier = Modifier.height(Dimens.SpaceS))

            PhoneInputField(
                phoneNumber = state.phoneNumber,
                isLoading = state.isLoading,
                onPhoneNumberChange = { onAction(LoginAction.PhoneNumberChanged(it)) },
                onRegionChange = { onAction(LoginAction.RegionChanged(it)) }
            )

            state.error?.let { error ->
                Spacer(modifier = Modifier.height(Dimens.SpaceS))
                Text(
                    text = error.asString(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceL))

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingWave(
                        color = MaterialTheme.colorScheme.primary,
                        height = Dimens.SpaceL,
                        barCount = 18,
                    )
                }
            } else {
                CareerPilotButton(
                    text = stringResource(id = R.string.btn_continue),
                    onClick = { onAction(LoginAction.SendOtpClicked) },
                    enabled = state.phoneNumber.isNotBlank()
                )
            }
        }
    }
}
