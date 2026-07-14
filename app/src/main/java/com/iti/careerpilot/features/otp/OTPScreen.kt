package com.iti.careerpilot.features.otp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun OTPRoot(
    openHome: () -> Unit,
    openRegister: () -> Unit,
    viewModel: OTPViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    OTPScreen(
        openHome = openHome,
        openRegister = openRegister,
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun OTPScreen(
    openHome: () -> Unit,
    openRegister: () -> Unit,
    state: OTPState,
    onAction: (OTPAction) -> Unit,
) {
    Column (
        modifier = Modifier
            .background(Color.Cyan)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "OTP Screen")
        Button(
            onClick = openHome
        ) {
            Text(text = "open home")
        }
        Button(
            onClick = openRegister
        ) {
            Text(text = "open Register")
        }
    }
}
