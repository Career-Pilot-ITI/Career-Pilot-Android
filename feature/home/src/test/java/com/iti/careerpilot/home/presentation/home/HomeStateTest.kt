package com.iti.careerpilot.home.presentation.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeStateTest {

    @Test
    fun `planLabel maps PLUS to Plus`() {
        val state = HomeState(subscriptionTier = "PLUS")
        assertEquals("Plus", state.planLabel)
    }

    @Test
    fun `planLabel maps PRO and MAX to Max`() {
        val proState = HomeState(subscriptionTier = "PRO")
        assertEquals("Max", proState.planLabel)

        val maxState = HomeState(subscriptionTier = "MAX")
        assertEquals("Max", maxState.planLabel)

        val lowerProState = HomeState(subscriptionTier = "pro")
        assertEquals("Max", lowerProState.planLabel)

        val lowerMaxState = HomeState(subscriptionTier = "max")
        assertEquals("Max", lowerMaxState.planLabel)
    }

    @Test
    fun `planLabel defaults to Free for other tiers`() {
        val freeState = HomeState(subscriptionTier = "FREE")
        assertEquals("Free", freeState.planLabel)

        val emptyState = HomeState(subscriptionTier = "")
        assertEquals("Free", emptyState.planLabel)
    }

    @Test
    fun `isSubscribed returns true for PLUS, PRO, and MAX`() {
        assertTrue(HomeState(subscriptionTier = "PLUS").isSubscribed)
        assertTrue(HomeState(subscriptionTier = "PRO").isSubscribed)
        assertTrue(HomeState(subscriptionTier = "MAX").isSubscribed)
        assertTrue(HomeState(subscriptionTier = "plus").isSubscribed)
        assertTrue(HomeState(subscriptionTier = "pro").isSubscribed)
        assertTrue(HomeState(subscriptionTier = "max").isSubscribed)

        assertFalse(HomeState(subscriptionTier = "FREE").isSubscribed)
        assertFalse(HomeState(subscriptionTier = "").isSubscribed)
    }
}
