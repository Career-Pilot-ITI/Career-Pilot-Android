package com.iti.careerpilot.quiz.presentation.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.quiz.R
import com.iti.careerpilot.quiz.domain.model.LearningQuiz

@Composable
fun QuizResultContent(
    quiz: LearningQuiz,
    answers: Map<Int, Int>,
    score: Int,
    onContinue: () -> Unit,
    onBackToTopics: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.SpaceXXL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL)
    ) {
        Text(
            text = stringResource(R.string.quiz_complete_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = stringResource(R.string.quiz_score_format, score, quiz.questions.size),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        val percentage = (score.toFloat() / quiz.questions.size * 100).toInt()
        Text(
            text = stringResource(R.string.quiz_percentage_format, percentage),
            style = MaterialTheme.typography.titleLarge,
            color = CareerPilotPalette.gray600
        )

        Spacer(modifier = Modifier.size(Dimens.SpaceL))

        quiz.questions.forEachIndexed { index, question ->
            val userAnswer = answers[index]
            val isCorrect = userAnswer == question.correctAnswerIndex

            CareerPilotCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = if (isCorrect) Color.Green.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.CardPadding),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
                ) {
                    Text(
                        text = stringResource(R.string.quiz_question_format, index + 1, question.question),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.quiz_your_answer, question.options[userAnswer ?: 0]),
                        color = if (isCorrect) Color.DarkGray else Color.Red
                    )
                    if (!isCorrect) {
                        Text(
                            text = stringResource(R.string.quiz_correct_answer, question.options[question.correctAnswerIndex]),
                            color = Color.Green
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

        Spacer(modifier = Modifier.size(Dimens.SpaceL))

        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.quiz_continue))
        }

        OutlinedButton(
            onClick = onBackToTopics,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.quiz_back_to_topics))
        }
    }
}
