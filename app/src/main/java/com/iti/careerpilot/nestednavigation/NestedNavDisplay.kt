package com.iti.careerpilot.nestednavigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.iti.careerpilot.ats.presentation.coverletter.view.CoverLetterRoot
import com.iti.careerpilot.ats.presentation.entry.view.AtsEntryRoot
import com.iti.careerpilot.ats.presentation.optimizedcv.view.OptimizedCvRoot
import com.iti.careerpilot.ats.presentation.scoring.view.ScoringRoot
import com.iti.careerpilot.home.presentation.home.screen.HomeRoot
import com.iti.careerpilot.profile.presentation.screen.ProfileRoot
import com.iti.careerpilot.reports.presentation.screen.history.view.SessionHistoryRoot
import com.iti.careerpilot.rootnavigation.Route
import com.iti.careerpilot.rootnavigation.navigateSingleTop
import com.iti.common.model.ProfileEditSection

import androidx.compose.ui.graphics.Color

@Composable
fun NestedNavDisplay(
    currentRootRoute: NavKey?,
    navigateBack: () -> Unit,
    openPaywall: (showGetCoins: Boolean) -> Unit,
    logout: () -> Unit,
    openPracticeSession: (Long, Long?) -> Unit,
    openSessionDetails: (Long) -> Unit,
    openSettings: () -> Unit,
    openEditProfile: (ProfileEditSection) -> Unit,
    openReadyToPractice: (trackId: Long, trackName: String, workspaceId: Long?) -> Unit,
    openInterviews: () -> Unit,
    pendingSharedText: String?,
    onSharedTextConsumed: () -> Unit,
) {

    val nestedBackStack = rememberNavBackStack(Route.NestedNav.Home)
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(pendingSharedText) {
        if (pendingSharedText != null) {
            nestedBackStack.apply {
                clear()
                add(Route.NestedNav.Home)
                add(Route.NestedNav.Ats)
            }
        }
    }

    Scaffold( // do not change window insets here
        containerColor = Color.Transparent,
        bottomBar = {
            if (nestedBackStack.lastOrNull().isBottomDestination()) {
                CareerPilotBottomNavBar(
                selectedIndex = nestedBackStack.selectedBottomNavBarIndex(),
                modifier = Modifier
            ) {
                BottomBarDestination.entries.forEach { destination ->
                    BottomNavBarItem(
                        modifier = Modifier.fillMaxSize(),
                        onClick = {
                            nestedBackStack.apply {
                                clear()
                                if (destination.route != Route.NestedNav.Home) {
                                    navigateSingleTop(Route.NestedNav.Home)
                                }
                                navigateSingleTop(destination.route)
                            }
                        },
                        isSelected = destination.route == nestedBackStack.lastOrNull(),
                        icon = destination.icon,
                        label = stringResource(destination.title),
                    )
                }
            }
            }
        }
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            backStack = nestedBackStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            onBack = {
                if (currentRootRoute == Route.NestedNav) {
                    nestedBackStack.removeLastOrNull()
                } else {
                    navigateBack()
                }
            },
            transitionSpec = {
                fadeIn(tween(350)) togetherWith fadeOut(tween(350))
            },
            entryProvider = entryProvider {
                entry<Route.NestedNav.Home> {
                    HomeRoot(
                        openReadyToPractice = { trackId, trackName ->
                            openReadyToPractice(trackId, trackName, null)
                        },
                        openSessionDetails = openSessionDetails,
                        openPracticeSession = { trackId, sessionId ->
                            openPracticeSession(trackId, sessionId)
                        },
                        openInterviews = openInterviews,
                        openPlansPaywall = { openPaywall(false) },
                        openCoinsPaywall = { openPaywall(true) },
                        openReports = {
                            nestedBackStack.apply {
                                clear()
                                navigateSingleTop(Route.NestedNav.Home)
                                navigateSingleTop(Route.NestedNav.SessionHistory)
                            }
                        },
                    )
                }
                entry<Route.NestedNav.SessionHistory> {
                    SessionHistoryRoot(
                        openSessionDetails = openSessionDetails,
                    )
                }
                entry<Route.NestedNav.Ats> {
                    AtsEntryRoot(
                        initialSharedText = pendingSharedText,
                        onSharedTextConsumed = onSharedTextConsumed,
                        onJobDetailsRequested = { workspaceId ->
                            nestedBackStack.navigateSingleTop(
                                Route.NestedNav.AtsWorkspace(workspaceId),
                            )
                        },
                    )
                }
                entry<Route.NestedNav.AtsWorkspace> { route ->
                    ScoringRoot(
                        workspaceId = route.workspaceId,
                        onBack = { nestedBackStack.removeLastOrNull() },
                        openCoinsPaywall = { openPaywall(true) },
                        openCoverLetter = { workspaceId ->
                            nestedBackStack.navigateSingleTop(
                                Route.NestedNav.AtsCoverLetter(workspaceId),
                            )
                        },
                        openOptimizedCv = { workspaceId ->
                            nestedBackStack.navigateSingleTop(
                                Route.NestedNav.AtsOptimizedCv(workspaceId),
                            )
                        },
                        openReadyToPractice = openReadyToPractice,
                        openJob = { url -> runCatching { uriHandler.openUri(url) } },
                    )
                }
                entry<Route.NestedNav.AtsCoverLetter> { route ->
                    CoverLetterRoot(
                        workspaceId = route.workspaceId,
                        onBack = { nestedBackStack.removeLastOrNull() },
                        openCoinsPaywall = { openPaywall(true) },
                    )
                }
                entry<Route.NestedNav.AtsOptimizedCv> { route ->
                    OptimizedCvRoot(
                        workspaceId = route.workspaceId,
                        onBack = { nestedBackStack.removeLastOrNull() },
                        openCoinsPaywall = { openPaywall(true) },
                    )
                }
                entry<Route.NestedNav.Profile> {
                    ProfileRoot(
                        openSettings = openSettings,
                        logout = logout,
                        openEditProfile = openEditProfile
                    )
                }
            }
        )
    }
}

private fun NavKey?.isBottomDestination() = this == Route.NestedNav.Home ||
    this == Route.NestedNav.SessionHistory ||
    this == Route.NestedNav.Ats ||
    this == Route.NestedNav.Profile

private fun NavBackStack<NavKey>.selectedBottomNavBarIndex(): Int {
    return this.lastOrNull()?.let {
        when (it) {
            Route.NestedNav.Home -> {
                0
            }

            Route.NestedNav.SessionHistory -> {
                2
            }

            Route.NestedNav.Ats -> {
                1
            }

            else -> {
                3
            }
        }
    } ?: 0
}
