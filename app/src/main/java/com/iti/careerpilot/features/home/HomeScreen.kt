package com.iti.careerpilot.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.CareerPilotPalette
import com.iti.careerpilot.core.designsystem.CareerPilotShapes
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.careerpilot.core.designsystem.components.CareerPilotCard
import com.iti.careerpilot.core.designsystem.components.ScoreRing

@Composable
fun HomeRoot(
    openPaywall: (Boolean) -> Unit,
    openPracticeSession: (Long, Long?) -> Unit,
    openSessionDetails: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.syncFromServer()
    }

    HomeScreen(
        openPaywall = openPaywall,
        openPracticeSession = openPracticeSession,
        openSessionDetails = openSessionDetails,
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun HomeScreen(
    openPaywall: (Boolean) -> Unit,
    openPracticeSession: (Long, Long?) -> Unit,
    openSessionDetails: (Long) -> Unit,
    state: HomeState,
    onAction: (HomeAction) -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            HomeTopBar(
                userName = state.userName,
                coinBalance = state.formattedCoinBalance,
                onCoinsClick = { openPaywall(false) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(Dimens.SpaceL),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL)
        ) {
            item {
                SubscriptionHeroCard(
                    sessionsUsed = state.trialSessionsUsed,
                    totalSessions = state.totalTrialSessions,
                    onUpgradeClick = { openPaywall(false) }
                )
            }

            item {
                OverallScoreCard(
                    score = state.overallScore,
                    feedback = state.scoreFeedback,
                    trend = state.scoreTrend
                )
            }

            item {
                PracticeInterviewCard(
                    onClick = { openPracticeSession(1, null) }
                )
            }

            item {
                SectionHeader(
                    title = "Recommended For You",
                    subtitle = "Based on your CV and Track",
                    onSeeAllClick = {}
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
                    contentPadding = PaddingValues(bottom = Dimens.SpaceS)
                ) {
                    items(state.recommendedSessions) { session ->
                        RecommendedSessionCard(
                            session = session,
                            onClick = { openPracticeSession(session.id, null) }
                        )
                    }
                }
            }

            item {
                SectionHeader(
                    title = "Recent Sessions",
                    onSeeAllClick = {}
                )
            }

            items(state.recentSessions) { session ->
                RecentSessionItem(
                    session = session,
                    onClick = { openSessionDetails(session.id) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(Dimens.SpaceXXL))
            }
        }
    }
}

@Composable
fun HomeTopBar(
    userName: String,
    coinBalance: String,
    onCoinsClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceM),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Good morning 👋",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Text(
                text = userName.ifBlank { "Career Pilot" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Surface(
            onClick = onCoinsClick,
            shape = CircleShape,
            color = CareerPilotPalette.amber.copy(alpha = 0.1f),
            modifier = Modifier.height(32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = Dimens.SpaceS)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = CareerPilotPalette.amber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = coinBalance,
                    style = MaterialTheme.typography.labelLarge,
                    color = CareerPilotPalette.amber,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SubscriptionHeroCard(
    sessionsUsed: Int,
    totalSessions: Int,
    onUpgradeClick: () -> Unit
) {
    val progress = if (totalSessions > 0) sessionsUsed.toFloat() / totalSessions else 0f
    val remaining = (totalSessions - sessionsUsed).coerceAtLeast(0)

    CareerPilotCard(
        containerColor = CareerPilotPalette.amber,
        useShadow = true
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpaceL)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FREE TRIAL",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "$remaining free session remaining",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Button(
                    onClick = onUpgradeClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(text = "Upgrade", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceM))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f),
            )

            Spacer(modifier = Modifier.height(Dimens.SpaceS))

            Text(
                text = "$sessionsUsed of $totalSessions free sessions used",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun OverallScoreCard(
    score: Int,
    feedback: String,
    trend: String
) {
    CareerPilotCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceL),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScoreRing(
                progress = score / 100f,
                progressColor = CareerPilotPalette.amber,
                trackColor = CareerPilotPalette.amber.copy(alpha = 0.1f),
                modifier = Modifier.size(70.dp),
                strokeWidthDp = 6.dp
            ) { animatedProgress ->
                Text(
                    text = (animatedProgress * 100).toInt().toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(Dimens.SpaceL))

            Column {
                Text(
                    text = "OVERALL SCORE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = feedback,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = CareerPilotPalette.green,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = trend,
                        style = MaterialTheme.typography.labelSmall,
                        color = CareerPilotPalette.green
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun PracticeInterviewCard(
    onClick: () -> Unit
) {
    CareerPilotCard(
        containerColor = CareerPilotPalette.navy,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceL),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = CareerPilotPalette.amber,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(Dimens.SpaceM))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SOFTWARE ENGINEERING",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    text = "Practice Interview",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CareerPilotPalette.amber,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    onSeeAllClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        Text(
            text = "See all",
            style = MaterialTheme.typography.labelMedium,
            color = CareerPilotPalette.amber,
            modifier = Modifier.clickable { onSeeAllClick() }
        )
    }
}

@Composable
fun RecommendedSessionCard(
    session: RecommendedSession,
    onClick: () -> Unit
) {
    CareerPilotCard(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpaceM)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CareerPilotPalette.teal.copy(alpha = 0.1f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Using a dummy icon for now
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = CareerPilotPalette.teal,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceM))

            Text(
                text = session.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(40.dp)
            )

            Spacer(modifier = Modifier.height(Dimens.SpaceS))

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = CareerPilotPalette.teal.copy(alpha = 0.05f),
                modifier = Modifier.wrapContentSize()
            ) {
                Text(
                    text = "Matches: ${session.matchReason}",
                    style = TextStyle(fontSize = 10.sp),
                    color = CareerPilotPalette.teal,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceM))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "~${session.durationMin} min",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CareerPilotPalette.amber,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentSessionItem(
    session: RecentSession,
    onClick: () -> Unit
) {
    CareerPilotCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CareerPilotPalette.green.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = session.score.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        color = CareerPilotPalette.green,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(Dimens.SpaceM))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${session.date} · ${session.durationMin} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
