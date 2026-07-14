package com.iti.careerpilot.features.login

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
fun LoginRoot(
    openOTP: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LoginScreen(
        openOTP = openOTP,
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun LoginScreen(
    openOTP: () -> Unit,
    state: LoginState,
    onAction: (LoginAction) -> Unit,
) {
    Column (
        modifier = Modifier
            .background(Color.LightGray)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "login Screen")
        Button(
            onClick = openOTP
        ) {
            Text(text = "open otp")
        }
    }
}
