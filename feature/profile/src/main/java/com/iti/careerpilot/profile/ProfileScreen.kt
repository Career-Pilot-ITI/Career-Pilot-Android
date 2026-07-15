package com.iti.careerpilot.profile

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
fun ProfileRoot(
    openSettings: () -> Unit,
    logout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ProfileScreen(
        openSettings = openSettings,
        logout = logout,
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun ProfileScreen(
    openSettings: () -> Unit,
    logout: () -> Unit,
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
) {
    Column (
        modifier = Modifier
            .background(Color.Magenta)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "profile Screen")
        Button(
            onClick = openSettings
        ) {
            Text(text = "open Settings")
        }
        Button(
            onClick = logout
        ) {
            Text(text = "Logout")
        }
    }
}
