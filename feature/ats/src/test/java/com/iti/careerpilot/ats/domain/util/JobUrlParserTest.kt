package com.iti.careerpilot.ats.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JobUrlParserTest {
    @Test
    fun `extracts first valid https URL and preserves query and fragment`() {
        val result = JobUrlParser.firstValidHttpsUrl(
            "Apply here: https://jobs.example.com/view/42?source=share#details then continue",
        )

        assertEquals("https://jobs.example.com/view/42?source=share#details", result)
    }

    @Test
    fun `skips invalid candidate and returns the next valid URL`() {
        val result = JobUrlParser.firstValidHttpsUrl(
            "https://localhost/job https://jobs.example.com/job",
        )

        assertEquals("https://jobs.example.com/job", result)
    }

    @Test
    fun `rejects unsupported or unsafe URL forms`() {
        assertFalse(JobUrlParser.isValidHttpsUrl("http://jobs.example.com/1"))
        assertFalse(JobUrlParser.isValidHttpsUrl("https://user:pass@jobs.example.com/1"))
        assertFalse(JobUrlParser.isValidHttpsUrl("https://localhost/1"))
        assertFalse(JobUrlParser.isValidHttpsUrl("https://127.0.0.1/1"))
        assertFalse(JobUrlParser.isValidHttpsUrl("https:///missing-host"))
        assertTrue(JobUrlParser.isValidHttpsUrl("https://jobs.example.com/1?x=1#apply"))
    }

    @Test
    fun `rejects excessive raw input and URL lengths`() {
        assertNull(JobUrlParser.firstValidHttpsUrl("x".repeat(JobUrlParser.MAX_RAW_INPUT_LENGTH + 1)))
        assertFalse(
            JobUrlParser.isValidHttpsUrl(
                "https://jobs.example.com/" + "x".repeat(JobUrlParser.MAX_URL_LENGTH),
            ),
        )
    }
}
