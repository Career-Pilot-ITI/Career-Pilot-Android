package com.iti.careerpilot.challengedashboard.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.challengedashboard.R
import com.iti.careerpilot.challengedashboard.presentation.action.ChallengeDashboardAction
import com.iti.careerpilot.challengedashboard.presentation.event.ChallengeDashboardEvent
import com.iti.careerpilot.challengedashboard.presentation.state.ChallengeDashboardState
import com.iti.careerpilot.challengedashboard.presentation.viewmodel.ChallengeDashboardViewModel
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.components.BackIconButton

@Composable
fun ChallengeDashboardScreenRoot(
    onBack: () -> Unit,
    viewModel: ChallengeDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvent(viewModel.events) { event ->
        when (event) {
            ChallengeDashboardEvent.NavigateBack -> onBack()
        }
    }

    ChallengeDashboardScreenContent(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun ChallengeDashboardScreenContent(
    state: ChallengeDashboardState,
    onAction: (ChallengeDashboardAction) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.challenge_dashboard_title),
                    )
                },
                navigationIcon = {
                    BackIconButton(onBack = { onAction(ChallengeDashboardAction.OnBackClicked) })
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Text("Challenge Dashboard")
        }
    }
}
