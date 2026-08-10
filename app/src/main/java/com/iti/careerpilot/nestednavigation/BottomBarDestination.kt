package com.iti.careerpilot.nestednavigation

import com.iti.careerpilot.R
import com.iti.careerpilot.rootnavigation.Route

enum class BottomBarDestination(
    val title: Int,
    val icon: Int,
    val route: Route
) {
    Home(
        title = R.string.home,
        icon = R.drawable.ic_home,
        route = Route.NestedNav.Home
    ),
    Ats(
        title = R.string.ats,
        icon = R.drawable.ic_ats,
        route = Route.NestedNav.Ats,
    ),
    Reports(
        title = R.string.reports,
        icon = R.drawable.ic_reports,
        route = Route.NestedNav.SessionHistory
    ),
    Profile(
        title = R.string.profile,
        icon = R.drawable.ic_person,
        route = Route.NestedNav.Profile
    )
}
