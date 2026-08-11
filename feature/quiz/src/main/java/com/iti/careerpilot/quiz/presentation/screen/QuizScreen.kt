package com.iti.careerpilot.quiz.presentation.screen

import androidx.compose.foundation.layout.Box
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
    viewModel: QuizViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveEvent(viewModel.events) { event ->
        // Handle events here
    }

    QuizScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun QuizScreen(
    state: QuizState,
    onAction: (QuizAction) -> Unit
) {
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Quiz Screen")
        }
    }
}
