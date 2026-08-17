package com.iti.careerpilot.share

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PendingChallengeDeepLinkTest {

    @Test
    fun `from with valid universal https uri returns PendingChallengeDeepLink`() {
        val uri = "https://career-pilot-indol.vercel.app/challenge?id=chl_999&code=INV123"
        val result = PendingChallengeDeepLink.from(uri)
        assertNotNull(result)
        assertEquals("chl_999", result?.challengeId)
        assertEquals("INV123", result?.invitationCode)
    }

    @Test
    fun `from with path param universal uri returns PendingChallengeDeepLink`() {
        val uri = "https://career-pilot-indol.vercel.app/challenge/chl_999"
        val result = PendingChallengeDeepLink.from(uri)
        assertNotNull(result)
        assertEquals("chl_999", result?.challengeId)
    }

    @Test
    fun `from with custom scheme uri returns PendingChallengeDeepLink`() {
        val uri = "careerpilot://challenge?id=chl_abc"
        val result = PendingChallengeDeepLink.from(uri)
        assertNotNull(result)
        assertEquals("chl_abc", result?.challengeId)
    }

    @Test
    fun `from with custom scheme path segment uri returns PendingChallengeDeepLink`() {
        val uri = "careerpilot://challenge/chl_abc"
        val result = PendingChallengeDeepLink.from(uri)
        assertNotNull(result)
        assertEquals("chl_abc", result?.challengeId)
    }

    @Test
    fun `from with custom scheme path segment and invitation code returns PendingChallengeDeepLink`() {
        val uri = "careerpilot://challenge/chl_abc?code=INV123"
        val result = PendingChallengeDeepLink.from(uri)
        assertNotNull(result)
        assertEquals("chl_abc", result?.challengeId)
        assertEquals("INV123", result?.invitationCode)
    }

    @Test
    fun `from with unrelated host returns null`() {
        val uri = "https://google.com/challenge?id=123"
        val result = PendingChallengeDeepLink.from(uri)
        assertNull(result)
    }

    @Test
    fun `from with missing challenge id returns null`() {
        assertNull(PendingChallengeDeepLink.from("https://career-pilot-indol.vercel.app/challenge"))
        assertNull(PendingChallengeDeepLink.from("https://career-pilot-indol.vercel.app/challenge?code=INV123"))
        assertNull(PendingChallengeDeepLink.from("careerpilot://challenge"))
        assertNull(PendingChallengeDeepLink.from("careerpilot://challenge?code=INV123"))
    }

    @Test
    fun `from with case insensitive scheme and host parses correctly`() {
        val uri = "HTTPS://CAREER-PILOT-INDOL.VERCEL.APP/challenge?id=CHL_123"
        val result = PendingChallengeDeepLink.from(uri)
        assertNotNull(result)
        assertEquals("CHL_123", result?.challengeId)
    }

    @Test
    fun `from with blank or invalid uri returns null`() {
        assertNull(PendingChallengeDeepLink.from(null))
        assertNull(PendingChallengeDeepLink.from(""))
        assertNull(PendingChallengeDeepLink.from("   "))
        assertNull(PendingChallengeDeepLink.from("not-a-valid-uri"))
    }
}
