package com.iti.careerpilot.ats.presentation.util

import com.iti.careerpilot.ats.domain.model.JobListing
import com.iti.careerpilot.ats.domain.model.JobWorkspace
import com.iti.common.util.UIText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class EmailDraftTest {
    private val fallback = UIText.DynamicString("Generic subject")

    @Test
    fun `subject prefers title and company and body remains exact edited value`() {
        val body = "Line one\nSymbols: & ? = +"
        val draft = coverLetterEmailDraft(workspace("Engineer", "Example"), body, fallback)

        assertEquals("Engineer — Example", (draft.subject as UIText.DynamicString).value)
        assertEquals(body, draft.body)
    }

    @Test
    fun `subject falls back to title only`() {
        val draft = coverLetterEmailDraft(workspace("Engineer", ""), "Body", fallback)

        assertEquals("Engineer", (draft.subject as UIText.DynamicString).value)
    }

    @Test
    fun `localized generic subject is retained when title is missing`() {
        val draft = coverLetterEmailDraft(workspace("", "Example"), "Body", fallback)

        assertSame(fallback, draft.subject)
    }

    private fun workspace(title: String, company: String) = JobWorkspace(
        id = 1,
        job = JobListing(
            id = 2,
            title = title,
            companyName = company,
            location = "",
            description = "",
            employmentType = null,
            seniorityLevel = null,
            requiredSkills = emptyList(),
            preferredSkills = emptyList(),
            responsibilities = emptyList(),
            qualifications = emptyList(),
            technologies = emptyList(),
            salaryMin = null,
            salaryMax = null,
            currency = null,
            experienceYears = null,
            educationLevel = null,
            applicationUrl = null,
            sourceUrl = null,
            sourceType = null,
        ),
        status = "IMPORTED",
        cvScore = null,
        cvScoreUpdatedAt = null,
        cvOptimizedText = null,
        coverLetterText = null,
        lastInterviewSessionId = null,
        createdAt = null,
        updatedAt = null,
    )
}
