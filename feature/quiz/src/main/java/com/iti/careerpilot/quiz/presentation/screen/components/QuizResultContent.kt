package com.iti.careerpilot.quiz.presentation.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.quiz.R
import com.iti.careerpilot.quiz.domain.model.LearningQuiz
import com.iti.careerpilot.quiz.domain.model.QuizQuestion

@Composable
fun QuizResultContent(
    quiz: LearningQuiz,
    answers: Map<Int, Int>,
    score: Int,
    onContinue: () -> Unit,
    onBackToTopics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val percentage by remember(score, quiz.questions.size) {
        derivedStateOf { (score * 100f / quiz.questions.size).toInt() }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL)
    ) {
        item {
            QuizScoreHeader(
                score = score,
                total = quiz.questions.size,
                percentage = percentage
            )
        }

        itemsIndexed(
            items = quiz.questions,
            key = { index, _ -> index }
        ) { index, question ->
            QuizQuestionResultCard(
                index = index,
                question = question,
                selectedAnswerIndex = answers[index]
            )
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
            ) {
                CareerPilotButton(
                    text = stringResource(R.string.quiz_continue),
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth()
                )
                CareerPilotButton(
                    text = stringResource(R.string.quiz_back_to_topics),
                    onClick = onBackToTopics,
                    variant = ButtonVariant.OUTLINE,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun QuizScoreHeader(
    score: Int,
    total: Int,
    percentage: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL)
    ) {
        Text(
            text = stringResource(R.string.quiz_complete_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.quiz_score_format, score, total),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.quiz_percentage_format, percentage),
            style = MaterialTheme.typography.titleLarge,
            color = CareerPilotPalette.gray600
        )
    }
}

@Composable
private fun QuizQuestionResultCard(
    index: Int,
    question: QuizQuestion,
    selectedAnswerIndex: Int?,
    modifier: Modifier = Modifier
) {
    val isCorrect = selectedAnswerIndex == question.correctAnswerIndex
    val wasAnswered = selectedAnswerIndex != null

    CareerPilotCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.CardPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
        ) {
            Text(
                text = stringResource(R.string.quiz_question_format, index + 1, question.question),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = if (wasAnswered) {
                    stringResource(R.string.quiz_your_answer, question.options[selectedAnswerIndex])
                } else {
                    stringResource(R.string.quiz_not_answered)
                },
                color = if (isCorrect) CareerPilotPalette.green else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold
            )

            if (!isCorrect) {
                Text(
                    text = stringResource(R.string.quiz_correct_answer, question.options[question.correctAnswerIndex]),
                    color = CareerPilotPalette.green,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = stringResource(R.string.quiz_explanation_header, question.explanation),
                style = MaterialTheme.typography.bodySmall,
                color = CareerPilotPalette.gray600
            )
        }
    }
}