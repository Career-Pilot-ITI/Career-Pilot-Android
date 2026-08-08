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
}
