package com.iti.careerpilot.nestednavigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.iti.careerpilot.challenges.presentation.screen.ChallengesScreenRoot
import com.iti.careerpilot.home.presentation.home.screen.HomeRoot
import com.iti.careerpilot.profile.presentation.screen.ProfileRoot
import com.iti.careerpilot.reports.presentation.screen.history.view.SessionHistoryRoot
import com.iti.careerpilot.rootnavigation.Route
import com.iti.careerpilot.rootnavigation.navigateSingleTop
import com.iti.common.model.ProfileEditSection

@Composable
fun NestedNavDisplay(
    currentRootRoute: NavKey?,
    navigateBack: () -> Unit,
    openPaywall: (showGetCoins: Boolean, showMySubscription: Boolean) -> Unit,
    logout: () -> Unit,
    openPracticeSession: (Long, Long?) -> Unit,
    openSessionDetails: (Long) -> Unit,
    openSettings: () -> Unit,
    openEditProfile: (ProfileEditSection) -> Unit,
    openReadyToPractice: (trackId: Long, trackName: String, workspaceId: Long?) -> Unit,
    openQuiz: (trackId: Long, trackName: String) -> Unit,
    openInterviews: () -> Unit,
    openAts: () -> Unit,
    openCreateChallenge: () -> Unit,
    openChallengeDashboard: () -> Unit,
    openChallengeDetails: (String) -> Unit,
) {

    val nestedBackStack = rememberNavBackStack(Route.NestedNav.Home)

    Scaffold( // do not change window insets here
        containerColor = Color.Transparent,
        bottomBar = {
            CareerPilotBottomNavBar(
                selectedIndex = nestedBackStack.selectedBottomNavBarIndex(),
                modifier = Modifier
            ) {
                BottomBarDestination.entries.forEach { destination ->
                    BottomNavBarItem(
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .fillMaxWidth()
                            .height(80.dp),
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
                        openQuiz = openQuiz,
                        openSessionDetails = openSessionDetails,
                        openPracticeSession = { trackId, sessionId ->
                            openPracticeSession(trackId, sessionId)
                        },
                        openInterviews = openInterviews,
                        openPlansPaywall = { showMySubscription -> openPaywall(false, showMySubscription) },
                        openCoinsPaywall = { openPaywall(true, false) },
                        openReports = {
                            nestedBackStack.apply {
                                clear()
                                navigateSingleTop(Route.NestedNav.Home)
                                navigateSingleTop(Route.NestedNav.SessionHistory)
                            }
                        },
                        openAts = openAts,
                    )
                }
                entry<Route.NestedNav.Challenges> {
                    ChallengesScreenRoot(
                        openCreateChallenge = openCreateChallenge,
                        openChallengeDashboard = openChallengeDashboard,
                        openChallengeDetails = openChallengeDetails
                    )
                }
                entry<Route.NestedNav.SessionHistory> {
                    SessionHistoryRoot(
                        openSessionDetails = openSessionDetails,
                    )
                }
                entry<Route.NestedNav.Profile> {
                    ProfileRoot(
                        openSettings = openSettings,
                        logout = logout,
                        openEditProfile = openEditProfile,
                        openSubscription = { openPaywall(false, true) }
                    )
                }
            }
        )
    }
}

private fun NavKey?.isBottomDestination() = this == Route.NestedNav.Home ||
        this == Route.NestedNav.Challenges ||
        this == Route.NestedNav.SessionHistory ||
        this == Route.NestedNav.Profile

private fun NavBackStack<NavKey>.selectedBottomNavBarIndex(): Int {
    return this.lastOrNull()?.let {
        when (it) {
            Route.NestedNav.Home -> {
                0
            }

            Route.NestedNav.Challenges -> {
                1
            }

            Route.NestedNav.SessionHistory -> {
                2
            }

            else -> {
                3
            }
        }
    } ?: 0
}
