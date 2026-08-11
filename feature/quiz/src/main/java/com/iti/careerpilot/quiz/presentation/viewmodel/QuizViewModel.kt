package com.iti.careerpilot.quiz.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.iti.careerpilot.quiz.domain.repository.QuizRepository
import com.iti.careerpilot.quiz.presentation.action.QuizAction
import com.iti.careerpilot.quiz.presentation.event.QuizEvent
import com.iti.careerpilot.quiz.presentation.state.QuizState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val quizRepo: QuizRepository
) : ViewModel() {

    private val _state = MutableStateFlow(QuizState())
    val state = _state.asStateFlow()

    private val _events = Channel<QuizEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: QuizAction) {
        when (action) {
            else -> {}
        }
    }
}