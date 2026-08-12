package com.iti.careerpilot.quiz.presentation.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.quiz.R
import com.iti.careerpilot.quiz.domain.model.LearningQuiz

@Composable
fun QuizContent(
    quiz: LearningQuiz,
    answers: Map<Int, Int>,
    onAnswerSelected: (Int, Int) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.SpaceXXL),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL)
    ) {
        Text(
            text = stringResource(R.string.quiz_check_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        quiz.questions.forEachIndexed { qIndex, question ->
            CareerPilotCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(Dimens.CardPadding),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
                ) {
                    Text(
                        text = stringResource(R.string.quiz_question_format, qIndex + 1, question.question),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Column(Modifier.selectableGroup()) {
                        question.options.forEachIndexed { oIndex, option ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = (answers[qIndex] == oIndex),
                                        onClick = { onAnswerSelected(qIndex, oIndex) },
                                        role = Role.RadioButton
                                    )
                                    .padding(vertical = Dimens.SpaceS),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (answers[qIndex] == oIndex),
                                    onClick = null // null recommended for accessibility with selectable modifier
                                )
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = Dimens.SpaceM)
                                )
                            }
                        }
                    }
                }
            }
        }

        val allAnswered = answers.size == quiz.questions.size
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = allAnswered
        ) {
            Text(text = stringResource(R.string.quiz_submit))
        }
    }
}
