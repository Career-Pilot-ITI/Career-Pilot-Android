package com.iti.careerpilot.quiz.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.quiz.presentation.action.QuizAction
import com.iti.careerpilot.quiz.presentation.state.QuizState
import com.iti.careerpilot.quiz.presentation.viewmodel.QuizViewModel

@Composable
fun QuizRoot(
    trackId: Long,
    trackName: String,
    onBack: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvent(viewModel.events) { event ->
        // Handle events here
    }

    QuizScreen(
        trackId = trackId,
        trackName = trackName,
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun QuizScreen(
    trackId: Long,
    trackName: String,
    state: QuizState,
    onAction: (QuizAction) -> Unit
) {
    Scaffold { innerPadding ->
        Column (
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Quiz Screen")
            Text(text = "id: $trackId")
            Text(text = "name: $trackName")
        }
    }
}
