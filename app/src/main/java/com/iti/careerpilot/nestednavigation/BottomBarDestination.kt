package com.iti.careerpilot.nestednavigation

import com.iti.careerpilot.R
import com.iti.careerpilot.rootnavigation.Route

enum class BottomBarDestination(
    val title: Int,
    val icon: Int,
    val selectedIcon: Int,
    val route: Route
) {
    Home(
        title = R.string.home,
        icon = R.drawable.ic_home,
        selectedIcon = R.drawable.ic_home_filled,
        route = Route.NestedNav.Home
    ),
    Reports(
        title = R.string.reports,
        icon = R.drawable.ic_reports,
        selectedIcon = R.drawable.ic_reports_filled,
        route = Route.NestedNav.Reports
    ),
    Profile(
        title = R.string.profile,
        icon = R.drawable.ic_profile,
        selectedIcon = R.drawable.ic_profile_filled,
        route = Route.NestedNav.Profile
    )
}