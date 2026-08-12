package com.iti.careerpilot.quiz.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.common.ObserveEvent
import com.iti.careerpilot.core.designsystem.components.BackIconButton
import com.iti.careerpilot.core.designsystem.components.LoadingWave
import com.iti.careerpilot.quiz.R
import com.iti.careerpilot.quiz.domain.model.StudyTopic
import com.iti.careerpilot.quiz.presentation.action.QuizAction
import com.iti.careerpilot.quiz.presentation.event.QuizEvent
import com.iti.careerpilot.quiz.presentation.screen.components.LearningPointContent
import com.iti.careerpilot.quiz.presentation.screen.components.QuizContent
import com.iti.careerpilot.quiz.presentation.screen.components.QuizResultContent
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
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(trackName) {
        viewModel.onAction(QuizAction.Init(trackName, state.seniority))
    }

    ObserveEvent(viewModel.events) { event ->
        when (event) {
            QuizEvent.QuizCompleted -> onBack()
            is QuizEvent.ShowError -> {
                snackbarHostState.showSnackbar(event.message.asString(context))
            }
        }
    }

    QuizScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack = onBack,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    state: QuizState,
    onAction: (QuizAction) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
// ...
            TopAppBar(
                title = {
                    Text(
                        text = if (state.currentStep == QuizStep.Topics) {
                            stringResource(R.string.quiz_learning_path)
                        } else {
                            state.selectedTopic?.title ?: stringResource(R.string.quiz_learning_default_title)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    BackIconButton(
                        onBack = {
                            if (state.currentStep == QuizStep.Topics) onBack()
                            else onAction(QuizAction.BackToTopics)
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (state.currentStep) {
                QuizStep.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingWave(color = MaterialTheme.colorScheme.primary)
                    }
                }

                QuizStep.Topics -> {
                    TopicsList(
                        topics = state.topics,
                        onTopicSelected = { onAction(QuizAction.TopicSelected(it)) }
                    )
                }

                QuizStep.LearningPoint -> {
                    state.currentLearningPoint?.let { lp ->
                        LearningPointContent(
                            learningPoint = lp,
                            onNext = { onAction(QuizAction.StartQuiz) }
                        )
                    }
                }

                QuizStep.Quiz -> {
                    state.currentQuiz?.let { quiz ->
                        QuizContent(
                            quiz = quiz,
                            answers = state.quizAnswers,
                            onAnswerSelected = { q, o -> onAction(QuizAction.AnswerSelected(q, o)) },
                            onSubmit = { onAction(QuizAction.SubmitQuiz) }
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
                            onBackToTopics = { onAction(QuizAction.BackToTopics) }
                        )
                    }
                }
            }

            if (state.isLoading && state.currentStep != QuizStep.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimens.SpaceL),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    LoadingWave(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun TopicsList(
    topics: List<StudyTopic>,
    onTopicSelected: (StudyTopic) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Dimens.SpaceL),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
    ) {
        item {
            Text(
                text = stringResource(R.string.quiz_topics_header),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = Dimens.SpaceS)
            )
        }
        items(topics, key = { it.id }) { topic ->
            TopicItem(
                topic = topic,
                onClick = { onTopicSelected(topic) }
            )
        }
    }
}
