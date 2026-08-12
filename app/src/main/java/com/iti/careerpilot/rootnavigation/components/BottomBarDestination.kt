package com.iti.careerpilot.rootnavigation.components

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
        route = Route.Home
    ),
    Ats(
        title = R.string.ats,
        icon = R.drawable.ic_ats,
        route = Route.Ats,
    ),
    Reports(
        title = R.string.reports,
        icon = R.drawable.ic_reports,
        route = Route.SessionHistory
    ),
    Profile(
        title = R.string.profile,
        icon = R.drawable.ic_person,
        route = Route.Profile
    )
}
