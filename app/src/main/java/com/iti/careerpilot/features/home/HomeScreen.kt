package com.iti.careerpilot.features.home

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
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun HomeRoot(
    openPaywall: (Boolean) -> Unit,
    openSessionDetails: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeScreen(
        openPaywall = openPaywall,
        openSessionDetails = openSessionDetails,
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun HomeScreen(
    openPaywall: (Boolean) -> Unit,
    openSessionDetails: (String) -> Unit,
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
        Text(text = state.formattedCoinBalance)
        Text(text = "Tier: ${state.formattedSubscriptionTier}")
        
        Button(
            onClick = { openPaywall(false) }
        ) {
            Text(text = "Open Paywall")
        }
        Button(
            onClick = { openPaywall(true) }
        ) {
            Text(text = "Test Get Coins")
        }
        Button(
            onClick = { openSessionDetails("132") }
        ) {
            Text(text = "Open Session Details")
        }
    }
}
