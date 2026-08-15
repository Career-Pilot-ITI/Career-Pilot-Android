package com.iti.careerpilot.createchallenge.presentation.screen

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
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.components.BackIconButton
import com.iti.careerpilot.createchallenge.R
import com.iti.careerpilot.createchallenge.presentation.action.CreateChallengeAction
import com.iti.careerpilot.createchallenge.presentation.event.CreateChallengeEvent
import com.iti.careerpilot.createchallenge.presentation.state.CreateChallengeState
import com.iti.careerpilot.createchallenge.presentation.viewmodel.CreateChallengeViewModel

@Composable
fun CreateChallengeScreenRoot(
    onBack: () -> Unit,
    viewModel: CreateChallengeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvent(viewModel.events) { event ->
        when (event) {
            CreateChallengeEvent.NavigateBack -> onBack()
        }
    }

    CreateChallengeScreenContent(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun CreateChallengeScreenContent(
    state: CreateChallengeState,
    onAction: (CreateChallengeAction) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.create_challenge_title),
                    )
                },
                navigationIcon = {
                    BackIconButton(onBack = { onAction(CreateChallengeAction.OnBackClicked) })
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
            Text("Create Challenge")
        }
    }
}
