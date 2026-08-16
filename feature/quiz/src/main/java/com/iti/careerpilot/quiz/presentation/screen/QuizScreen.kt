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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.iti.careerpilot.core.designsystem.components.CoinTopUpBottomSheet
import com.iti.careerpilot.core.designsystem.components.FeatureGateBottomSheet
import com.iti.careerpilot.core.designsystem.components.LoadingDialog
import com.iti.careerpilot.quiz.R
import com.iti.careerpilot.quiz.domain.model.StudyTopic
import com.iti.careerpilot.quiz.presentation.action.QuizIntent
import com.iti.careerpilot.quiz.presentation.event.QuizEffect
import com.iti.careerpilot.quiz.presentation.screen.components.ErrorContent
import com.iti.careerpilot.quiz.presentation.screen.components.LearningPointContent
import com.iti.careerpilot.quiz.presentation.screen.components.QuizContent
import com.iti.careerpilot.quiz.presentation.screen.components.QuizResultContent
import com.iti.careerpilot.quiz.presentation.screen.components.SelectSeniorityContent
import com.iti.careerpilot.quiz.presentation.screen.components.TopicItem
import com.iti.careerpilot.quiz.presentation.state.QuizState
import com.iti.careerpilot.quiz.presentation.state.QuizStep
import com.iti.careerpilot.quiz.presentation.viewmodel.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizRoot(
    trackId: Long,
    trackName: String,
    onBack: () -> Unit,
    openPaywall: (showGetCoins: Boolean) -> Unit = {},
    viewModel: QuizViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(trackName) {
        viewModel.onIntent(QuizIntent.Init(trackName))
    }

    ObserveEvent(viewModel.events) { event ->
        when (event) {
            QuizEffect.QuizCompleted -> onBack()
            is QuizEffect.NavigateToPaywall -> openPaywall(event.showGetCoins)
        }
    }

    QuizScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack,
    )

    if (state.showGateSheet) {
        FeatureGateBottomSheet(
            featureName = stringResource(R.string.quiz_feature_name),
            requiredPlan = state.gateRequiredPlan,
            planFeatures = state.gatePlanFeatures,
            onUpgradeClick = { viewModel.onIntent(QuizIntent.UpgradeFromGate) },
            onDismiss = { viewModel.onIntent(QuizIntent.DismissGateSheet) },
        )
    }

    if (state.showCoinTopUpSheet) {
        CoinTopUpBottomSheet(
            coinCost = state.coinTopUpRequiredCost,
            currentBalance = state.coinBalance,
            onBuyCoins = { viewModel.onIntent(QuizIntent.BuyCoinsClicked) },
            onDismiss = { viewModel.onIntent(QuizIntent.DismissCoinTopUpSheet) },
        )
    }
}

@Composable
fun QuizScreen(
    state: QuizState,
    onIntent: (QuizIntent) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            QuizTopBar(state = state, onBack = onBack, onIntent = onIntent)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            QuizStepContent(
                state = state,
                onIntent = onIntent,
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
    onIntent: (QuizIntent) -> Unit
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
                        QuizStep.Topics -> onIntent(QuizIntent.BackToSeniority)
                        else -> onIntent(QuizIntent.BackToTopics)
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
    onIntent: (QuizIntent) -> Unit,
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
                    state = state,
                    onIntent = onIntent,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            QuizStep.Topics -> {
                TopicsList(
                    topics = state.topics,
                    onTopicSelected = { onIntent(QuizIntent.TopicSelected(it)) },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            QuizStep.LearningPoint -> {
                state.currentLearningPoint?.let { learningPoint ->
                    LearningPointContent(
                        learningPoint = learningPoint,
                        onNext = { onIntent(QuizIntent.StartQuiz) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            QuizStep.Quiz -> {
                state.currentQuiz?.let { quiz ->
                    QuizContent(
                        quiz = quiz,
                        answers = state.quizAnswers,
                        onAnswerSelected = { q, o -> onIntent(QuizIntent.AnswerSelected(q, o)) },
                        onSubmit = { onIntent(QuizIntent.SubmitQuiz) },
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
                        onContinue = { onIntent(QuizIntent.ContinueLearning) },
                        onBackToTopics = { onIntent(QuizIntent.BackToTopics) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            QuizStep.Error -> {
                ErrorContent(
                    error = state.error,
                    onRetry = { onIntent(QuizIntent.Retry) },
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
