package com.iti.careerpilot.ats.domain.util

import com.iti.careerpilot.ats.domain.model.JobListing
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class JobDisplayMetadataResolverTest {
    @Test
    fun `prefers explicit experience and recognizes explicit hybrid wording`() {
        val result = JobDisplayMetadataResolver.resolve(
            job(description = "This hybrid role asks for 9 years of experience", experienceYears = 5),
        )
        assertEquals(5, result.experienceYears)
        assertEquals(WorkArrangement.HYBRID, result.workArrangement)
    }

    @Test
    fun `does not invent metadata from vague copy`() {
        val result = JobDisplayMetadataResolver.resolve(
            job(description = "Join a flexible team with meaningful opportunities"),
        )
        assertNull(result.experienceYears)
        assertNull(result.workArrangement)
    }

    private fun job(description: String, experienceYears: Int? = null) = JobListing(
        id = 1L,
        title = "Engineer",
        companyName = "Example",
        location = "Cairo",
        description = description,
        employmentType = null,
        seniorityLevel = null,
        requiredSkills = persistentListOf(),
        preferredSkills = persistentListOf(),
        responsibilities = persistentListOf(),
        qualifications = persistentListOf(),
        technologies = persistentListOf(),
        salaryMin = null,
        salaryMax = null,
        currency = null,
        experienceYears = experienceYears,
        educationLevel = null,
        applicationUrl = null,
        sourceUrl = null,
        sourceType = null,
    )
}
