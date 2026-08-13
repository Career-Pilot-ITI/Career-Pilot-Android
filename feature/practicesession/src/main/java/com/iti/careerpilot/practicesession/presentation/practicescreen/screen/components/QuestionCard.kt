package com.iti.careerpilot.practicesession.presentation.practicescreen.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.practicesession.R

@Composable
fun QuestionCard(
    questionOrder: Int?,
    questionText: String?,
    isReadingQuestion: Boolean,
    onPlayClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CareerPilotCard(
        elevation = 8.dp,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .heightIn(max = 115.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.question_number, questionOrder ?: ""),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                if (isReadingQuestion) {
                    IconButton(
                        onClick = onStopClick,
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_stop),
                            contentDescription = stringResource(R.string.stop)
                        )
                    }
                } else {
                    IconButton(
                        onClick = onPlayClick,
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_play),
                            contentDescription = stringResource(R.string.play)
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 65.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = questionText ?: stringResource(R.string.loading_question),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
