package com.iti.careerpilot.reports.presentation.screen.breakdown.view.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.reports.R
import com.iti.careerpilot.reports.presentation.screen.breakdown.uimodels.QuestionUiModel

@Composable
fun QuestionSelector(
    questions: List<QuestionUiModel>,
    selectedQuestionId: String?,
    onQuestionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val selectedIndex = remember(questions, selectedQuestionId) {
        questions.indexOfFirst { it.id == selectedQuestionId }
    }
    LaunchedEffect(selectedIndex) {
        if (selectedIndex >= 0) listState.animateScrollToItem(selectedIndex)
    }
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
    ) {
        items(
            items = questions,
            key = { it.id },
            contentType = { "question-selector" },
        ) { question ->
            val isSelected = question.id == selectedQuestionId
            Surface(
                modifier = Modifier
                    .size(Dimens.MinimumTouchTarget)
                    .semantics { selected = isSelected }
                    .clip(RoundedCornerShape(Dimens.SpaceXL))
                    .clickable(
                        role = Role.Tab,
                        onClick = { onQuestionSelected(question.id) },
                    ),
                shape = RoundedCornerShape(Dimens.SpaceXL),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    MaterialTheme.colorScheme.surface
                },
                contentColor = if (isSelected) {
                    MaterialTheme.colorScheme.background
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                },
                border = if (isSelected) null else BorderStroke(
                    Dimens.RadarGridStrokeWidth,
                    MaterialTheme.colorScheme.outline,
                ),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.reports_question_number, question.index),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}