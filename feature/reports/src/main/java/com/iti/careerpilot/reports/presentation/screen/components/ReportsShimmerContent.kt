package com.iti.careerpilot.reports.presentation.screen.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.shimmerLoading
import com.iti.careerpilot.reports.R

@Composable
fun SessionHistoryShimmerContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.SpaceXL),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
    ) {
        LoadingLabel(messageRes = R.string.reports_loading_history)
        ShimmerBlock(height = Dimens.SpaceL)
        repeat(HISTORY_CARD_PLACEHOLDER_COUNT) {
            ShimmerBlock(height = Dimens.QuestionMetricHeight)
        }
    }
}

@Composable
fun ReportDetailsShimmerContent(
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Dimens.SpaceXL),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
    ) {
        item {
            LoadingLabel(messageRes = R.string.reports_loading_details)
        }
        item {
            ShimmerBlock(height = Dimens.RadarChartContainerSize)
        }
        item {
            ShimmerBlock(height = Dimens.RadarChartContainerSize)
        }
        item {
            ShimmerBlock(height = Dimens.ButtonHeight)
        }
        items(DETAIL_CARD_PLACEHOLDER_COUNT) {
            ShimmerBlock(height = Dimens.ScoreRingSize)
        }
    }
}

@Composable
fun QuestionBreakdownShimmerContent(
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(Dimens.SpaceXL),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
    ) {
        item {
            LoadingLabel(messageRes = R.string.reports_loading_questions)
        }
        item {
            ShimmerBlock(height = Dimens.ButtonHeight)
        }
        item {
            ShimmerBlock(height = Dimens.QuestionMetricHeight)
        }
        item {
            ShimmerBlock(height = Dimens.QuestionMetricHeight)
        }
        items(BREAKDOWN_CARD_PLACEHOLDER_COUNT) {
            ShimmerBlock(height = Dimens.ScoreRingSize)
        }
    }
}

@Composable
private fun LoadingLabel(
    @StringRes messageRes: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(messageRes),
        modifier = modifier.padding(vertical = Dimens.SpaceS),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = LOADING_LABEL_ALPHA),
    )
}

@Composable
private fun ShimmerBlock(
    height: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .shimmerLoading(),
    )
}

private const val HISTORY_CARD_PLACEHOLDER_COUNT = 5
private const val DETAIL_CARD_PLACEHOLDER_COUNT = 3
private const val BREAKDOWN_CARD_PLACEHOLDER_COUNT = 2
private const val LOADING_LABEL_ALPHA = 0.72f
