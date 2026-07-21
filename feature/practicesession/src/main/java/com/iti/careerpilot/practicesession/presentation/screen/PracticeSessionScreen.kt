package com.iti.careerpilot.practicesession.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.practicesession.presentation.action.PracticeSessionAction
import com.iti.careerpilot.practicesession.presentation.state.PracticeSessionState
import com.iti.careerpilot.practicesession.presentation.viewmodel.PracticeSessionViewModel

@Composable
fun PracticeSessionRoot(
    sessionId: String,
    viewModel: PracticeSessionViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.onAction(PracticeSessionAction.StartPracticeSession(sessionId))
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    PracticeSessionScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun PracticeSessionScreen(
    state: PracticeSessionState,
    onAction: (PracticeSessionAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .background(Color.Green)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Session Details Screen")
    }
}