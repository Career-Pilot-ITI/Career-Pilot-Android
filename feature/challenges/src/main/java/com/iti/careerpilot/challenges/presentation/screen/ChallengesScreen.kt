package com.iti.careerpilot.challenges.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.challenges.presentation.action.ChallengesAction
import com.iti.careerpilot.challenges.presentation.event.ChallengesEvent
import com.iti.careerpilot.challenges.presentation.state.ChallengesState
import com.iti.careerpilot.challenges.presentation.viewmodel.ChallengesViewModel
import com.iti.careerpilot.core.designsystem.common.ObserveEvent

@Composable
fun ChallengesScreenRoot(
    viewModel: ChallengesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvent(viewModel.events) { event ->
        when (event) {
            ChallengesEvent.NavigateToCreateChallenge -> {}
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
}
