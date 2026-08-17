package com.iti.common.util

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ChallengeShareHelperTest {

    @Test
    fun `buildShareUrl produces valid universal url with id and code`() {
        val url = ChallengeShareHelper.buildShareUrl("ch_123", "CODE456")
        assertEquals("https://career-pilot-indol.vercel.app/challenge?id=ch_123&code=CODE456", url)
    }

    @Test
    fun `buildShareUrl with blank code produces url with id only`() {
        val url = ChallengeShareHelper.buildShareUrl("ch_123", null)
        assertEquals("https://career-pilot-indol.vercel.app/challenge?id=ch_123", url)
    }

    @Test
    fun `buildShareUrl when invitation code matches challenge id produces url with id only`() {
        val url = ChallengeShareHelper.buildShareUrl("ch_123", "ch_123")
        assertEquals("https://career-pilot-indol.vercel.app/challenge?id=ch_123", url)
    }

    @Test
    fun `buildShareMessage formats simple professional message without emojis`() {
        val message = ChallengeShareHelper.buildShareMessage(
            creatorName = "Hazem",
            trackName = "Android Development",
            seniorityLevel = "Junior",
            challengeType = "Audio & Video",
            questionsCount = 5,
            challengeId = "ch_123",
            invitationCode = "CODE456"
        )

        assertTrue(message.contains("Career Pilot Challenge Invitation"))
        assertTrue(message.contains("Hazem"))
        assertTrue(message.contains("Android Development"))
        assertTrue(message.contains("Junior"))
        assertTrue(message.contains("Audio & Video"))
        assertTrue(message.contains("5 questions"))
        assertTrue(message.contains("https://career-pilot-indol.vercel.app/challenge?id=ch_123&code=CODE456"))
        assertTrue(message.contains("CODE456"))

        // Verify no emojis are present
        val hasEmoji = message.any { Character.isSurrogate(it) }
        assertFalse("Message must not contain emojis", hasEmoji)
    }

    @Test
    fun `createShareTextIntent creates valid send intent`() {
        val intent = ChallengeShareHelper.createShareTextIntent("Hello Challenge")
        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("text/plain", intent.type)
        assertEquals("Hello Challenge", intent.getStringExtra(Intent.EXTRA_TEXT))
    }

    @Test
    fun `createShareChallengeIntent with null bitmap falls back to text intent`() {
        val context = RuntimeEnvironment.getApplication()
        val intent = ChallengeShareHelper.createShareChallengeIntent(context, "Hello Challenge", null)
        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("text/plain", intent.type)
        assertEquals("Hello Challenge", intent.getStringExtra(Intent.EXTRA_TEXT))
    }
}
