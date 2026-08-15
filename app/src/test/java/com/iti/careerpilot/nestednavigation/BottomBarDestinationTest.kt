package com.iti.careerpilot.nestednavigation

import org.junit.Assert.assertEquals
import org.junit.Test

class BottomBarDestinationTest {

    @Test
    fun bottomBarContainsOnlyPrimaryDestinations() {
        assertEquals(
            listOf(
                BottomBarDestination.Home,
                BottomBarDestination.Reports,
                BottomBarDestination.Profile,
            ),
            BottomBarDestination.entries,
        )
    }
}
