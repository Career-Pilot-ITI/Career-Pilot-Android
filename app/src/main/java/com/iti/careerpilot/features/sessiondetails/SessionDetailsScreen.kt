package com.iti.careerpilot.features.sessiondetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun SessionDetailsRoot(
    sessionId: String,
    viewModel: SessionDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SessionDetailsScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun SessionDetailsScreen(
    state: SessionDetailsState,
    onAction: (SessionDetailsAction) -> Unit,
) {
    Column (
        modifier = Modifier
            .background(Color.Green)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Session Details Screen")
    }
}
