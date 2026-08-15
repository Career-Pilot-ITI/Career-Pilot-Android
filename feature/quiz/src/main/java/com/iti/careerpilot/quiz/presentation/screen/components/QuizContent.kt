package com.iti.careerpilot.quiz.presentation.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotShapes
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.quiz.R
import com.iti.careerpilot.quiz.domain.model.LearningQuiz
import com.iti.careerpilot.quiz.domain.model.QuizQuestion

@Composable
fun QuizContent(
    quiz: LearningQuiz,
    answers: Map<Int, Int>,
    onAnswerSelected: (Int, Int) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allAnswered by remember(answers, quiz.questions.size) {
        derivedStateOf { answers.size == quiz.questions.size }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL)
    ) {
        item {
            Text(
                text = parseMarkdown(stringResource(R.string.quiz_check_title)),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        itemsIndexed(
            items = quiz.questions,
            key = { index, _ -> index }
        ) { qIndex, question ->
            QuizQuestionCard(
                index = qIndex,
                question = question,
                selectedOptionIndex = answers[qIndex],
                onOptionSelected = { oIndex -> onAnswerSelected(qIndex, oIndex) }
            )
        }

        item {
            CareerPilotButton(
                text = stringResource(R.string.quiz_submit),
                onClick = onSubmit,
                enabled = allAnswered,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun QuizQuestionCard(
    index: Int,
    question: QuizQuestion,
    selectedOptionIndex: Int?,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    CareerPilotCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Dimens.CardPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
        ) {
            Text(
                text = parseMarkdown(stringResource(R.string.quiz_question_format, index + 1, question.question)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(Modifier.selectableGroup()) {
                question.options.forEachIndexed { oIndex, option ->
                    QuizOptionRow(
                        text = option,
                        isSelected = selectedOptionIndex == oIndex,
                        onClick = { onOptionSelected(oIndex) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizOptionRow(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CareerPilotShapes.small)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                else Color.Transparent
            )
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(horizontal = Dimens.SpaceS, vertical = Dimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = parseMarkdown(text),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = Dimens.SpaceM),
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
