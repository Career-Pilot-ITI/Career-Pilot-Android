package com.iti.careerpilot.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun HomeRoot(
    openPaywall: () -> Unit,
    openPracticeSession: (Long, Long?) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeScreen(
        openPaywall = openPaywall,
        openSessionDetails = openPracticeSession,
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun HomeScreen(
    openPaywall: () -> Unit,
    openSessionDetails: (Long, Long?) -> Unit,
    state: HomeState,
    onAction: (HomeAction) -> Unit,
) {
    Column (
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Home Screen")
        Button(
            onClick = openPaywall
        ) {
            Text(text = "open Paywall")
        }
        Button(
            onClick = { openSessionDetails(1, null) }
        ) {
            Text(text = "Open Practice Session")
        }
    }
}
