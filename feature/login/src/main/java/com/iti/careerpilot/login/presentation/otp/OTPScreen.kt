package com.iti.careerpilot.login.presentation.otp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun OTPRoot(
    phoneNumber: String,
    openHome: () -> Unit,
    openRegister: () -> Unit,
    viewModel: OTPViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(phoneNumber) {
        viewModel.onAction(OTPAction.PhoneNumberReceived(phoneNumber))
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is OTPEvent.NavigateToHome -> openHome()
            }
        }
    }

    OTPScreen(
        openRegister = openRegister,
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun OTPScreen(
    openRegister: () -> Unit,
    state: OTPState,
    onAction: (OTPAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .background(Color.Cyan)
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "OTP Screen")
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "Code sent to ${state.phoneNumber}",
        )

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            value = state.code,
            onValueChange = { onAction(OTPAction.CodeChanged(it)) },
            label = { Text(text = "OTP code") },
            placeholder = { Text(text = "123456") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            enabled = !state.isLoading,
        )

        state.error?.let { error ->
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                text = error.asString(),
                color = MaterialTheme.colorScheme.error,
            )
        }

        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { onAction(OTPAction.VerifyClicked) },
                enabled = state.code.isNotBlank(),
            ) {
                Text(text = "Verify")
            }
            TextButton(
                onClick = { onAction(OTPAction.ResendClicked) }
            ) {
                Text(text = "Resend code")
            }
            TextButton(
                onClick = openRegister
            ) {
                Text(text = "open Register")
            }
        }
    }
}
