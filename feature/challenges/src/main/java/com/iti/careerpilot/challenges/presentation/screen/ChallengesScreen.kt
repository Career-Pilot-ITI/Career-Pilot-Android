package com.iti.careerpilot.challenges.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.challenges.presentation.action.ChallengesAction
import com.iti.careerpilot.challenges.presentation.event.ChallengesEvent
import com.iti.careerpilot.challenges.presentation.state.ChallengesState
import com.iti.careerpilot.challenges.presentation.viewmodel.ChallengesViewModel
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton

@Composable
fun ChallengesScreenRoot(
    openCreateChallenge: () -> Unit,
    viewModel: ChallengesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvent(viewModel.events) { event ->
        when (event) {
            ChallengesEvent.NavigateToCreateChallenge -> openCreateChallenge()
        }
    }

    ChallengesScreenContent(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun ChallengesScreenContent(
    state: ChallengesState,
    onAction: (ChallengesAction) -> Unit
) {
    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.navigationBars),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Text("Challenges Screen")
            CareerPilotButton(
                text = "Open create challenge",
                onClick = {
                    onAction(ChallengesAction.CreateChallengeClicked)
                }
            )
        }
    }
}
