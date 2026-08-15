package com.iti.careerpilot.quiz.presentation.screen.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
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
            text = parseMarkdown(learningPoint.title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = parseMarkdown(learningPoint.explanation),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        LearningExampleBlock(
            example = learningPoint.example,
            isCode = learningPoint.isCode,
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
    isCode: Boolean,
    modifier: Modifier = Modifier
) {
    val extendedColors = CareerPilotTheme.extendedColors
    val containerColor = extendedColors.codeBackground
    val contentColor = extendedColors.codeForeground

    CareerPilotCard(
        modifier = modifier,
        containerColor = containerColor
    ) {
        Column(
            modifier = Modifier
                .padding(Dimens.CardPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
        ) {
            Text(
                text = stringResource(R.string.quiz_example_header),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor.copy(alpha = 0.7f)
            )

            val textModifier = if (isCode) {
                Modifier.horizontalScroll(rememberScrollState())
            } else {
                Modifier
            }

            Text(
                text = parseMarkdown(
                    text = example,
                    inlineCodeColor = if (isCode) contentColor else extendedColors.studioTextPrimary,
                    inlineCodeBackgroundColor = if (isCode) Color.Transparent else extendedColors.studioSurfaceElevated2
                ),
                color = contentColor,
                fontFamily = if (isCode) FontFamily.Monospace else FontFamily.Default,
                style = MaterialTheme.typography.bodyMedium,
                softWrap = !isCode,
                modifier = textModifier
            )
        }
    }
}
