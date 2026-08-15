package com.iti.careerpilot.challengedetails.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.challengedetails.R
import com.iti.careerpilot.challengedetails.presentation.action.ChallengeDetailsAction
import com.iti.careerpilot.challengedetails.presentation.event.ChallengeDetailsEvent
import com.iti.careerpilot.challengedetails.presentation.state.ChallengeDetailsState
import com.iti.careerpilot.challengedetails.presentation.viewmodel.ChallengeDetailsViewModel
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.components.BackIconButton


@Composable
fun ChallengeDetailsScreenRoot(
    challengeId: String,
    onBack: () -> Unit,
    viewModel: ChallengeDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvent(viewModel.events) { event ->
        when (event) {
            ChallengeDetailsEvent.NavigateBack -> onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.challenge_details_title)) },
                navigationIcon = {
                    BackIconButton(onBack = { viewModel.onAction(ChallengeDetailsAction.OnBackClicked) })
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        ChallengeDetailsScreenContent(
            modifier = Modifier.padding(padding),
            state = state,
            onAction = viewModel::onAction
        )
    }
}

@Composable
fun ChallengeDetailsScreenContent(
    modifier: Modifier = Modifier,
    state: ChallengeDetailsState,
    onAction: (ChallengeDetailsAction) -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text("Challenge Details")
    }
}
