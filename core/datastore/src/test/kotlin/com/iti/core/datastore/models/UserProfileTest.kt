package com.iti.core.datastore.models

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserProfileTest {

    @Test
    fun `AccountInfo bodyLanguageConsentGiven defaults to false`() {
        val account = AccountInfo()
        assertFalse(account.bodyLanguageConsentGiven)
    }

    @Test
    fun `AccountInfo bodyLanguageConsentGiven can be updated`() {
        val account = AccountInfo(bodyLanguageConsentGiven = true)
        assertTrue(account.bodyLanguageConsentGiven)
    }

    @Test
    fun `isPaidSubscriber returns true for PLUS, PRO, and MAX tiers regardless of casing`() {
        listOf("PLUS", "PRO", "MAX", "plus", "pro", "max", "Plus", "Pro", "Max").forEach { tier ->
            val profile = UserProfile(account = AccountInfo(subscriptionTier = tier))
            assertTrue("Expected tier $tier to be recognized as paid subscriber", profile.isPaidSubscriber())
        }
    }

    @Test
    fun `isPaidSubscriber returns false for FREE, blank, or invalid tiers`() {
        listOf("FREE", "free", "", "starter", "BASIC", "basic", "trial").forEach { tier ->
            val profile = UserProfile(account = AccountInfo(subscriptionTier = tier))
            assertFalse("Expected tier $tier to not be recognized as paid subscriber", profile.isPaidSubscriber())
        }
    }
}
