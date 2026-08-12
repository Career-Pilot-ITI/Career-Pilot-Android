package com.iti.careerpilot.quiz.presentation.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotShapes
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.quiz.R
import com.iti.careerpilot.quiz.domain.model.LearningPoint

@Composable
fun LearningPointContent(
    learningPoint: LearningPoint,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
    ) {
        Text(
            text = learningPoint.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = learningPoint.explanation,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        LearningExampleBlock(
            example = learningPoint.example,
            modifier = Modifier.padding(top = Dimens.SpaceM)
        )

        Spacer(modifier = Modifier.size(Dimens.SpaceXXL))

        CareerPilotButton(
            text = stringResource(R.string.quiz_start_check),
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LearningExampleBlock(
    example: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
    ) {
        Text(
            text = stringResource(R.string.quiz_example_header),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CareerPilotShapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(Dimens.SpaceL)
        ) {
            Text(
                text = example,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}