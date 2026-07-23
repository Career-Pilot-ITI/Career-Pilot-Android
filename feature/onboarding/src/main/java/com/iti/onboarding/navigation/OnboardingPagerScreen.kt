package com.iti.onboarding.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Velocity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iti.careerpilot.core.designsystem.Dimens
import com.iti.careerpilot.core.designsystem.components.CareerPilotButton
import com.iti.onboarding.R
import com.iti.onboarding.presentation.screen.cv.state.UploadCvIntent
import com.iti.onboarding.presentation.screen.cv.view.UploadCvScreen
import com.iti.onboarding.presentation.screen.cv.viewmodel.UploadCvViewModel
import com.iti.onboarding.presentation.screen.profileinfo.view.ProfileInfoScreen
import com.iti.onboarding.presentation.screen.profileinfo.viewmodel.ProfileInfoIntent
import com.iti.onboarding.presentation.screen.profileinfo.viewmodel.ProfileInfoViewModel
import com.iti.onboarding.presentation.screen.track.state.ChoosingTracksIntent
import com.iti.onboarding.presentation.screen.track.view.ChoosingTracksScreen
import com.iti.onboarding.presentation.screen.track.viewmodel.ChoosingTracksViewModel
import kotlinx.coroutines.launch

private const val PAGE_UPLOAD_CV = 0
private const val PAGE_PROFILE_INFO = 1
private const val PAGE_CHOOSING_TRACKS = 2
private const val ONBOARDING_PAGE_COUNT = 3

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnboardingPagerScreen(
    onOnboardingFinished: () -> Unit,
    cvViewModel: UploadCvViewModel = hiltViewModel(),
    profileViewModel: ProfileInfoViewModel = hiltViewModel(),
    tracksViewModel: ChoosingTracksViewModel = hiltViewModel(),
) {
    val pagerState = rememberPagerState(
        initialPage = PAGE_UPLOAD_CV,
        pageCount = { ONBOARDING_PAGE_COUNT }
    )
    val scope = rememberCoroutineScope()

    val cvState by cvViewModel.state.collectAsStateWithLifecycle()
    val profileState by profileViewModel.state.collectAsStateWithLifecycle()
    val tracksState by tracksViewModel.state.collectAsStateWithLifecycle()

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val scrollConnection = remember(isRtl) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val isForward = if (isRtl) available.x > 0 else available.x < 0
                return if (isForward) Offset(available.x, 0f) else Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val isForward = if (isRtl) available.x > 0 else available.x < 0
                return if (isForward) Velocity(available.x, 0f) else Velocity.Zero
            }
        }
    }

    BackHandler(enabled = pagerState.currentPage > 0) {
        scope.launch {
            pagerState.animateScrollToPage(pagerState.currentPage - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start
        ) {
            PagerHeader(currentPage = pagerState.currentPage)

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .nestedScroll(scrollConnection),
                userScrollEnabled = false,
            ) { page ->
                when (page) {
                    PAGE_UPLOAD_CV -> UploadCvScreen(
                        viewModel = cvViewModel,
                        onNavigateNext = {
                            scope.launch { pagerState.animateScrollToPage(PAGE_PROFILE_INFO) }
                        },
                        onSkip = {
                            scope.launch { pagerState.animateScrollToPage(PAGE_PROFILE_INFO) }
                        }
                    )

                    PAGE_PROFILE_INFO -> ProfileInfoScreen(
                        viewModel = profileViewModel,
                        onNavigateNext = {
                            scope.launch { pagerState.animateScrollToPage(PAGE_CHOOSING_TRACKS) }
                        }
                    )

                    PAGE_CHOOSING_TRACKS -> ChoosingTracksScreen(
                        viewModel = tracksViewModel,
                        onNavigateNext = onOnboardingFinished
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .windowInsetsPadding(WindowInsets.navigationBars)
                .windowInsetsPadding(WindowInsets.ime)
                .padding(top = Dimens.SpaceXXXL)
        ) {
            val currentPage = pagerState.currentPage

            val buttonText = when (currentPage) {
                PAGE_UPLOAD_CV -> stringResource(R.string.analyze_my_cv)
                PAGE_PROFILE_INFO -> stringResource(R.string.next_button_label)
                PAGE_CHOOSING_TRACKS -> stringResource(R.string.next_button_label)
                else -> ""
            }

            val isButtonEnabled = when (currentPage) {
                PAGE_UPLOAD_CV -> cvState.isFormValid
                PAGE_PROFILE_INFO -> profileState.isFormValid
                PAGE_CHOOSING_TRACKS -> tracksState.isFormValid
                else -> false
            }

            val isSubmitting = when (currentPage) {
                PAGE_UPLOAD_CV -> cvState.isSubmitting
                PAGE_PROFILE_INFO -> profileState.isSubmitting
                PAGE_CHOOSING_TRACKS -> false
                else -> false
            }

            val onClick = {
                when (currentPage) {
                    PAGE_UPLOAD_CV -> cvViewModel.onIntent(UploadCvIntent.OnAnalyzeClick)
                    PAGE_PROFILE_INFO -> profileViewModel.onIntent(ProfileInfoIntent.OnSubmit)
                    PAGE_CHOOSING_TRACKS -> tracksViewModel.onIntent(ChoosingTracksIntent.OnNavigateNext)
                }
            }

            CareerPilotButton(
                text = buttonText,
                enabled = isButtonEnabled && !isSubmitting,
                onClick = onClick,
                modifier = Modifier
                    .padding(horizontal = Dimens.SpaceXXL)
                    .padding(bottom = Dimens.SpaceXXL)
            )
        }
    }
}

@Composable
private fun PagerHeader(currentPage: Int) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(vertical = Dimens.SpaceL, horizontal = Dimens.SpaceM),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        PagerIndicator(
            pageCount = ONBOARDING_PAGE_COUNT,
            currentPage = currentPage,
        )

        Text(
            stringResource(R.string.pager_header, currentPage + 1, ONBOARDING_PAGE_COUNT),
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onBackground.copy(
                    alpha = 0.7f
                )
            )
        )
    }
}

@Composable
private fun PagerIndicator(pageCount: Int, currentPage: Int) {
    Row(
        modifier = Modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { iteration ->
            val isSelected = currentPage == iteration

            val color by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                },
                label = "color"
            )

            val width by animateDpAsState(
                targetValue = if (isSelected) Dimens.SpaceXXL else Dimens.SpaceS,
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
                label = "width"
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = Dimens.SpaceXS)
                    .height(Dimens.SpaceS)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
