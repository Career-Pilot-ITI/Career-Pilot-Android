package com.iti.careerpilot.ats.data.mapper

import com.iti.careerpilot.ats.data.dto.AtsScoreDto
import com.iti.careerpilot.ats.data.dto.AtsApiResponseDto
import com.iti.careerpilot.ats.data.dto.AiJobDto
import com.iti.careerpilot.ats.data.dto.AtsSectionScoreDto
import com.iti.careerpilot.ats.data.dto.JobDto
import com.iti.careerpilot.ats.data.dto.JobWorkspaceDto
import kotlinx.serialization.json.Json
import com.iti.careerpilot.ats.domain.model.AiJobStatus
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

    @Test
    fun `pending optimization keeps result absent even when backend returns empty object`() {
        val response = Json.decodeFromString<AtsApiResponseDto<AiJobDto>>(
            """
            {
              "success": true,
              "data": {
                "id": 42,
                "workspaceId": 7,
                "type": "CV_OPTIMIZE",
                "status": "PENDING",
                "progressPercentage": 0,
                "currentStep": "Queued",
                "result": {}
              }
            }
            """.trimIndent(),
        )

        val mapped = requireNotNull(response.data).toDomain()

        assertEquals(AiJobStatus.PENDING, mapped.status)
        assertNull(mapped.result)
    }

    @Test
    fun `completed optimization maps section improvements and empty sections`() {
        val response = Json.decodeFromString<AtsApiResponseDto<AiJobDto>>(
            """
            {
              "success": true,
              "data": {
                "id": 42,
                "workspaceId": 7,
                "type": "CV_OPTIMIZE",
                "status": "COMPLETED",
                "progressPercentage": 100,
                "currentStep": "Done",
                "result": {
                  "sections": [
                    {
                      "name": "Experience",
                      "score": 82,
                      "improvements": [
                        {
                          "original": "Worked on APIs.",
                          "improved": "Delivered 12 APIs.",
                          "reason": "Adds measurable impact."
                        }
                      ]
                    },
                    {
                      "name": "Skills",
                      "score": 90,
                      "improvements": []
                    }
                  ],
                  "recommendedTracks": [" Backend Development ", ""],
                  "coinCost": 50
                }
              }
            }
            """.trimIndent(),
        )

        val mapped = requireNotNull(response.data).toDomain()
        val optimization = requireNotNull(mapped.result)

        assertEquals(AiJobStatus.COMPLETED, mapped.status)
        assertEquals(2, optimization.sections.size)
        assertEquals("Delivered 12 APIs.", optimization.sections.first().improvements.single().improved)
        assertEquals(emptyList<Any>(), optimization.sections.last().improvements)
        assertEquals(listOf("Backend Development"), optimization.recommendedTracks)
        assertEquals(50, optimization.coinCost)
    }
}
