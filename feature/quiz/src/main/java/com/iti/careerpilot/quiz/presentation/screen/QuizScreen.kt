package com.iti.careerpilot.quiz.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.components.BackIconButton
import com.iti.careerpilot.core.designsystem.components.LoadingDialog
import com.iti.careerpilot.quiz.R
import com.iti.careerpilot.quiz.domain.model.StudyTopic
import com.iti.careerpilot.quiz.presentation.action.QuizAction
import com.iti.careerpilot.quiz.presentation.event.QuizEvent
import com.iti.careerpilot.quiz.presentation.screen.components.ErrorContent
import com.iti.careerpilot.quiz.presentation.screen.components.LearningPointContent
import com.iti.careerpilot.quiz.presentation.screen.components.QuizContent
import com.iti.careerpilot.quiz.presentation.screen.components.QuizResultContent
import com.iti.careerpilot.quiz.presentation.screen.components.SelectSeniorityContent
import com.iti.careerpilot.quiz.presentation.screen.components.TopicItem
import com.iti.careerpilot.quiz.presentation.state.QuizState
import com.iti.careerpilot.quiz.presentation.state.QuizStep
import com.iti.careerpilot.quiz.presentation.viewmodel.QuizViewModel

@Composable
fun QuizRoot(
    trackId: Long,
    trackName: String,
    onBack: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(trackName) {
        viewModel.onAction(QuizAction.Init(trackName))
    }

    ObserveEvent(viewModel.events) { event ->
        when (event) {
            QuizEvent.QuizCompleted -> onBack()
        }
    }

    QuizScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack = onBack,
    )
}

@Composable
fun QuizScreen(
    state: QuizState,
    onAction: (QuizAction) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            QuizTopBar(state = state, onBack = onBack, onAction = onAction)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            QuizStepContent(
                state = state,
                onAction = onAction,
                modifier = Modifier.fillMaxSize()
            )

            if (state.isLoading) {
                LoadingDialog(
                    title = state.loadingMessage?.asString() ?: ""
                )
            }
        }
    }
}

@Composable
private fun QuizTopBar(
    state: QuizState,
    onBack: () -> Unit,
    onAction: (QuizAction) -> Unit
) {
    val isAtTopics = state.currentStep == QuizStep.Topics
    val isAtSeniority = state.currentStep == QuizStep.SelectSeniority

    TopAppBar(
        title = {
            Text(
                text = when {
                    isAtSeniority -> stringResource(R.string.quiz_select_seniority)
                    isAtTopics -> stringResource(R.string.quiz_learning_path)
                    else -> state.selectedTopic?.title
                        ?: stringResource(R.string.empty_string)
                },
            )
        },
        navigationIcon = {
            BackIconButton(
                onBack = {
                    when (state.currentStep) {
                        QuizStep.SelectSeniority -> onBack()
                        QuizStep.Topics -> onAction(QuizAction.BackToSeniority)
                        else -> onAction(QuizAction.BackToTopics)
                    }
                }
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
private fun QuizStepContent(
    state: QuizState,
    onAction: (QuizAction) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = state.currentStep,
        modifier = modifier,
    ) { step ->
        when (step) {
            QuizStep.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // LoadingDialog will be shown as an overlay
                }
            }

            QuizStep.SelectSeniority -> {
                SelectSeniorityContent(
                    onSenioritySelected = { level -> onAction(QuizAction.SenioritySelected(level.apiKey)) },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            QuizStep.Topics -> {
                TopicsList(
                    topics = state.topics,
                    onTopicSelected = { onAction(QuizAction.TopicSelected(it)) },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            QuizStep.LearningPoint -> {
                state.currentLearningPoint?.let { learningPoint ->
                    LearningPointContent(
                        learningPoint = learningPoint,
                        onNext = { onAction(QuizAction.StartQuiz) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            QuizStep.Quiz -> {
                state.currentQuiz?.let { quiz ->
                    QuizContent(
                        quiz = quiz,
                        answers = state.quizAnswers,
                        onAnswerSelected = { q, o -> onAction(QuizAction.AnswerSelected(q, o)) },
                        onSubmit = { onAction(QuizAction.SubmitQuiz) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            QuizStep.QuizResult -> {
                state.currentQuiz?.let { quiz ->
                    QuizResultContent(
                        quiz = quiz,
                        answers = state.quizAnswers,
                        score = state.quizScore,
                        onContinue = { onAction(QuizAction.ContinueLearning) },
                        onBackToTopics = { onAction(QuizAction.BackToTopics) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            QuizStep.Error -> {
                ErrorContent(
                    error = state.error,
                    onRetry = { onAction(QuizAction.Retry) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                )
            }
        }
    }
}

@Composable
fun TopicsList(
    topics: List<StudyTopic>,
    onTopicSelected: (StudyTopic) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
    ) {
        items(topics, key = { it.id }) { topic ->
            TopicItem(
                topic = topic,
                onClick = { onTopicSelected(topic) }
            )
        }
    }
}
