package com.iti.careerpilot.quiz.presentation.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.iti.careerpilot.core.designsystem.CareerPilotShapes
import com.iti.careerpilot.core.designsystem.Dimens
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
            .padding(Dimens.SpaceXXL)
    ) {
        Text(
            text = learningPoint.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.size(Dimens.SpaceM))
        Text(
            text = learningPoint.explanation,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.size(Dimens.SpaceL))
        Text(
            text = stringResource(R.string.quiz_example_header),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.size(Dimens.SpaceS))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CareerPilotShapes.medium)
                .background(Color.DarkGray)
                .padding(Dimens.SpaceL)
        ) {
            Text(
                text = learningPoint.example,
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.size(Dimens.SpaceXXL))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.quiz_start_check))
        }
    }
}
