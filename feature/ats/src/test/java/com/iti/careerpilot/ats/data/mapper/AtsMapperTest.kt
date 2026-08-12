package com.iti.careerpilot.ats.data.mapper

import com.iti.careerpilot.ats.data.dto.AtsScoreDto
import com.iti.careerpilot.ats.data.dto.AtsSectionScoreDto
import com.iti.careerpilot.ats.data.dto.JobDto
import com.iti.careerpilot.ats.data.dto.JobWorkspaceDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AtsMapperTest {
    @Test
    fun `workspace mapping preserves optional backend absence`() {
        val mapped = JobWorkspaceDto(
            id = 7L,
            job = JobDto(id = 9L, title = " Engineer ", requiredSkills = listOf(" Kotlin ", "")),
        ).toDomain()

        assertEquals(7L, mapped.id)
        assertEquals(" Engineer ", mapped.job.title)
        assertEquals(listOf("Kotlin"), mapped.job.requiredSkills)
        assertNull(mapped.job.employmentType)
        assertNull(mapped.job.applicationUrl)
        assertNull(mapped.cvScore)
    }

    @Test
    fun `score mapping bounds presentation scores and removes blank list values`() {
        val mapped = AtsScoreDto(
            overallScore = 140,
            matchPercentage = -5,
            matchedSkills = listOf(" Kotlin ", " "),
            sections = listOf(AtsSectionScoreDto("Projects", -1, "Feedback")),
            coinCost = -2,
        ).toDomain()

        assertEquals(100, mapped.overallScore)
        assertEquals(0, mapped.matchPercentage)
        assertEquals(listOf("Kotlin"), mapped.matchedSkills)
        assertEquals(0, mapped.sections.single().score)
        assertEquals(0, mapped.coinCost)
    }

    @Test
    fun `job response accepts text sections and maps display metadata`() {
        val dto = Json.decodeFromString<JobDto>(
            """
            {
              "id": 9,
              "responsibilities": "Build accessible products",
              "qualifications": ["Five years experience"],
              "companyLogoUrl": "https://example.com/logo.png",
              "postedLabel": "2 days ago",
              "applicantsLabel": "1000+ Person"
            }
            """.trimIndent(),
        )

        val mapped = dto.toDomain()

        assertEquals(listOf("Build accessible products"), mapped.responsibilities)
        assertEquals(listOf("Five years experience"), mapped.qualifications)
        assertEquals("https://example.com/logo.png", mapped.companyLogoUrl)
        assertEquals("2 days ago", mapped.postedLabel)
        assertEquals("1000+ Person", mapped.applicantsLabel)
    }
}
