package com.iti.careerpilot.practicesession.presentation.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.iti.careerpilot.practicesession.R

@Composable
fun QuestionCard(
    questionOrder: Int?,
    questionText: String?,
    onPlayClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.question_number, questionOrder ?: ""),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Row {
                    IconButton(onClick = onPlayClick) {
                        Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.play))
                    }
                    IconButton(onClick = onStopClick) {
                        Icon(Icons.Default.Stop, contentDescription = stringResource(R.string.stop))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = questionText ?: stringResource(R.string.loading_question),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                maxLines = 4
            )
        }
    }
}
