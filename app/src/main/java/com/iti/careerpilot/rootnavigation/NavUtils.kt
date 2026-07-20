package com.iti.careerpilot.rootnavigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

fun <T : NavKey> NavBackStack<T>.navigateSingleTop(
    route: T
) {
    if (lastOrNull() != route) {
        add(route)
    }
}

inline fun <reified T: NavKey> NavBackStack<*>.popIfCurrentIs() {
    if (lastOrNull() is T) {
        removeLastOrNull()
    }
}

fun <T : NavKey> NavBackStack<T>.replaceAll(
    route: T
) {
    if (isNotEmpty()) {
        this[0] = route
        while (size > 1) {
            removeAt(1)
        }
    } else {
        add(route)
    }
}