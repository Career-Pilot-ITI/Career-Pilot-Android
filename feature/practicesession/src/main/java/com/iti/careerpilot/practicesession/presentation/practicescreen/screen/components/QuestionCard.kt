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

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun QuestionCard(
    questionOrder: Int?,
    questionText: String?,
    isReadingQuestion: Boolean,
    onPlayClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    CareerPilotCard(
        elevation = 8.dp,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .heightIn(max = 135.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                    shape = CircleShape,
                ) {
                    Text(
                        text = stringResource(R.string.question_number, questionOrder ?: ""),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = if (isReadingQuestion) onStopClick else onPlayClick,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(
                            if (isReadingQuestion) R.drawable.ic_stop else R.drawable.ic_play
                        ),
                        contentDescription = stringResource(
                            if (isReadingQuestion) R.string.stop else R.string.play
                        ),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 70.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = questionText ?: stringResource(R.string.loading_question),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Start
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Subtle bottom gradient fade hint when text exceeds visible height
                if (scrollState.canScrollForward) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                                    )
                                )
                            )
                    )
                }
            }

            // Explicit UX indicator notifying the user that the question is scrollable
            AnimatedVisibility(
                visible = scrollState.canScrollForward,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_expand_down),
                        contentDescription = stringResource(R.string.scroll_for_more),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.80f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.scroll_for_more),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.80f)
                    )
                }
            }
        }
    }
}
