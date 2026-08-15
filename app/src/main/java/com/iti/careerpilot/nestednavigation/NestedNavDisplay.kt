package com.iti.careerpilot.nestednavigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
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
    openReadyToPractice: (trackId: Long, trackName: String) -> Unit,
    openInterviews: () -> Unit,
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
                        openReadyToPractice = openReadyToPractice,
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
                        openPracticeSession = { trackId, sessionId ->
                            openPracticeSession(trackId, sessionId)
                        },
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

private fun NavBackStack<NavKey>.selectedBottomNavBarIndex(): Int {
    return this.lastOrNull()?.let {
        when (it) {
            Route.NestedNav.Home -> {
                0
            }

            Route.NestedNav.SessionHistory -> {
                1
            }

            else -> {
                2
            }
        }
    } ?: 0
}
