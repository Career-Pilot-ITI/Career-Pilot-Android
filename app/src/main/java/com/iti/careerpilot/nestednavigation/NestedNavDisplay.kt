package com.iti.careerpilot.nestednavigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.iti.careerpilot.features.reports.ReportsRoot
import com.iti.careerpilot.features.home.HomeRoot
import com.iti.careerpilot.profile.presentation.screen.ProfileRoot
import com.iti.careerpilot.rootnavigation.Route
import com.iti.careerpilot.rootnavigation.navigateSingleTop

@Composable
fun NestedNavDisplay(
    currentRootRoute: NavKey?,
    navigateBack: () -> Unit,
    openPaywall: () -> Unit,
    logout: () -> Unit,
    openSessionDetails: (String) -> Unit,
    openSettings: () -> Unit,
    openEditProfile: () -> Unit,
) {

    val nestedBackStack = rememberNavBackStack(Route.NestedNav.Home)

    Scaffold(
        bottomBar = {
            ShortNavigationBar {
                BottomBarDestination.entries.forEach { destination ->
                    val isSelected = nestedBackStack.lastOrNull() == destination.route
                    BottomNavigationButton(
                        onClick = {
                            nestedBackStack.apply {
                                clear()
                                if (destination.route != Route.NestedNav.Home) {
                                    navigateSingleTop(Route.NestedNav.Home)
                                }
                                navigateSingleTop(destination.route)
                            }
                        },
                        icon = if (isSelected) destination.selectedIcon else destination.icon,
                        modifier = Modifier,
                        selected = isSelected,
                        label = destination.title
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
                        openSessionDetails = openSessionDetails,
                        openPaywall = openPaywall
                    )
                }
                entry<Route.NestedNav.Reports> {
                    ReportsRoot()
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