package com.iti.careerpilot.practicesession.presentation.resultscreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.common.GradientIcon
import com.iti.careerpilot.core.designsystem.components.BackIconButton
import com.iti.careerpilot.core.designsystem.components.ButtonVariant
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.core.designsystem.components.ScoreRing
import com.iti.careerpilot.practicesession.R
import com.iti.careerpilot.practicesession.domain.models.SessionQuestionResult
import com.iti.careerpilot.practicesession.domain.models.SessionResult
import com.iti.careerpilot.practicesession.presentation.resultscreen.components.BodyLanguageSection
import com.iti.careerpilot.practicesession.presentation.resultscreen.components.KeyMomentsTimeline

@Composable
fun ResultRoot(
    sessionId: Long,
    bodyLanguageMetricsJson: String? = null,
    onBack: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.onAction(ResultAction.UpdateSessionId(sessionId, bodyLanguageMetricsJson))
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    ResultScreen(
        state = state,
        onBack = onBack,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    state: ResultState,
    onBack: () -> Unit,
    onAction: (ResultAction) -> Unit,
) {
    val pullToRefreshState = rememberPullToRefreshState()
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackIconButton(onBack = onBack)
                },
                title = {
                    Text(
                        text = stringResource(R.string.session_results),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = state.isLoading,
            onRefresh = { onAction(ResultAction.RefreshResult) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    state = pullToRefreshState,
                    isRefreshing = state.isLoading,
                    modifier = Modifier.align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            }
        ) {
            state.sessionResult?.let { result ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimens.SpaceXL),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXXL)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(Dimens.SpaceS))
                        OverallScoreSection(result.overallScore)
                    }

                    item {
                        ScoreBreakdownSection(result)
                    }

                    if (state.bodyLanguageUiState !is BodyLanguageUiState.Idle || state.bodyLanguageMetrics != null) {
                        item {
                            BodyLanguageSection(
                                uiState = state.bodyLanguageUiState,
                                metrics = state.bodyLanguageMetrics,
                            )
                        }
                        state.bodyLanguageMetrics?.keyMoments?.let { keyMoments ->
                            if (keyMoments.isNotEmpty()) {
                                item {
                                    KeyMomentsTimeline(keyMoments = keyMoments)
                                }
                            }
                        }
                    }

                    if (result.coachingTips.isNotEmpty()) {
                        item {
                            CoachingTipsSection(result.coachingTips)
                        }
                    }

                    item {
                        Text(
                            text = stringResource(R.string.question_details),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    items(result.questions) { question ->
                        QuestionDetailItem(question)
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            } ?: run {
                if (!state.isLoading) {
                    ResultErrorState(
                        onRetry = { onAction(ResultAction.RefreshResult) }
                    )
                }
            }
        }
    }
}

@Composable
fun ResultErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens.SpaceXXL),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_error),
            contentDescription = null,
            modifier = Modifier.size(Dimens.ErrorRingSize),
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
        )
        
        Spacer(modifier = Modifier.height(Dimens.SpaceXXL))
        
        Text(
            text = stringResource(R.string.no_results_found),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(Dimens.SpaceS))
        
        Text(
            text = stringResource(R.string.error_creating_results),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(Dimens.SpaceXXXL))
        
        CareerPilotButton(
            text = stringResource(R.string.retry),
            onClick = onRetry,
            modifier = Modifier.padding(horizontal = Dimens.SpaceXXXL)
        )
    }
}

@Composable
fun OverallScoreSection(score: Int) {
    val isGoodResult = score >= 70
    val isExcellentResult = score >= 85

    val infiniteTransition = rememberInfiniteTransition(label = "celebration")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.AnimationBoxSize)
                .drawBehind {
                    if (isGoodResult) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = if (isExcellentResult) glowAlpha else 0.2f),
                                    Color.Transparent
                                )
                            ),
                            radius = size.minDimension * 0.8f
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            ScoreRing(
                progress = score / 100f,
                progressColor = if (isGoodResult) primaryColor else MaterialTheme.colorScheme.error,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxSize(),
                strokeWidthDp = Dimens.SpaceM,
                centerContent = { animatedProgress ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (animatedProgress * 100).toInt().toString(),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 48.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "/ 100",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )

            if (isExcellentResult) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Dimens.SpaceL)
                        .size(Dimens.SpaceXXXL),
                    tint = secondaryColor
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.SpaceL))

        Text(
            text = when {
                score >= 85 -> stringResource(R.string.excellent_performance)
                score >= 70 -> stringResource(R.string.good_performance)
                else -> stringResource(R.string.keep_practicing)
            },
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ScoreBreakdownSection(result: SessionResult) {
    CareerPilotCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpaceXL),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL)
        ) {
            Text(
                text = stringResource(R.string.performance_breakdown),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            ScoreItem(
                label = stringResource(R.string.clarity),
                score = result.clarityScore,
                icon = Icons.Default.RecordVoiceOver
            )
            ScoreItem(
                label = stringResource(R.string.confidence),
                score = result.confidenceScore,
                icon = Icons.Default.Psychology
            )
            ScoreItem(
                label = stringResource(R.string.pacing),
                score = result.pacingScore,
                icon = Icons.Default.Speed
            )
            ScoreItem(
                label = stringResource(R.string.filler_words),
                score = result.fillerWordsScore,
                icon = Icons.Default.Timer
            )
            ScoreItem(
                label = stringResource(R.string.content_relevance),
                score = result.contentRelevanceScore,
                icon = Icons.AutoMirrored.Filled.TrendingUp
            )
        }
    }
}

@Composable
fun ScoreItem(label: String, score: Int, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.SpaceXXXXL)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            GradientIcon(
                icon = icon
            )
        }

        Spacer(modifier = Modifier.width(Dimens.SpaceM))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = label, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "$score%",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(Dimens.SpaceXS))
            LinearProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun CoachingTipsSection(tips: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(Dimens.SpaceS))
            Text(
                text = stringResource(R.string.coaching_tips),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        tips.forEach { tip ->
            CareerPilotCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(Dimens.SpaceL),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(Dimens.SpaceXL)
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpaceM))
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun QuestionDetailItem(question: SessionQuestionResult) {
    var expanded by remember { mutableStateOf(false) }

    CareerPilotCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = { expanded = !expanded },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
    ) {
        Column(modifier = Modifier.padding(Dimens.SpaceL)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.question_number, question.questionOrder),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                
                question.score?.let {
                    Text(
                        text = "${it.overallScore}%",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (it.overallScore >= 70) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Dimens.SpaceS))
            
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(Dimens.SpaceL))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(Dimens.SpaceL))
                    
                    Text(
                        text = stringResource(R.string.your_transcript),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = question.userTranscript.ifBlank { stringResource(R.string.no_transcript_available) },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = Dimens.SpaceXS)
                    )
                    
                    question.score?.let { score ->
                        Spacer(modifier = Modifier.height(Dimens.SpaceL))
                        Text(
                            text = stringResource(R.string.analysis),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(Dimens.SpaceS))
                        
                        SmallScoreItem(stringResource(R.string.clarity), score.clarity)
                        SmallScoreItem(stringResource(R.string.confidence), score.confidence)
                        SmallScoreItem(stringResource(R.string.pacing), score.pacing)
                        SmallScoreItem(stringResource(R.string.filler_words), score.fillerWords)
                        SmallScoreItem(stringResource(R.string.content_relevance), score.contentRelevance)
                        
                        if (score.coachingTip.isNotBlank()) {
                            Spacer(modifier = Modifier.height(Dimens.SpaceM))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(Dimens.SpaceS))
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                                    .padding(Dimens.SpaceM)
                            ) {
                                Row {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        modifier = Modifier.size(Dimens.SpaceL),
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.width(Dimens.SpaceS))
                                    Text(
                                        text = score.coachingTip,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceS))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text(
                    text = stringResource(R.string.more_details),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceS))
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun SmallScoreItem(label: String, score: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.SpaceXS),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier
                    .width(60.dp)
                    .height(Dimens.SpaceXS)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.width(Dimens.SpaceS))
            Text(
                text = "$score%",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
