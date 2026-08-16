package com.iti.careerpilot.ats.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.iti.careerpilot.ats.domain.model.AtsScore
import com.iti.careerpilot.ats.domain.model.AtsSectionScore
import com.iti.careerpilot.ats.domain.model.CvOptimizationSection
import com.iti.careerpilot.ats.domain.model.CvSectionImprovement
import com.iti.careerpilot.ats.domain.model.JobListing
import com.iti.careerpilot.ats.domain.model.JobWorkspace
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterIntent
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterUiState
import com.iti.careerpilot.ats.presentation.coverletter.view.CoverLetterScreen
import com.iti.careerpilot.ats.presentation.entry.state.AtsEntryIntent
import com.iti.careerpilot.ats.presentation.entry.view.AtsEntryScreen
import com.iti.careerpilot.ats.presentation.entry.state.AtsEntryUiState
import com.iti.careerpilot.ats.presentation.jobdetails.view.JobDetailsScreen
import com.iti.careerpilot.ats.presentation.jobdetails.state.JobDetailsIntent
import com.iti.careerpilot.ats.presentation.jobdetails.state.JobDetailsUiState
import com.iti.careerpilot.ats.presentation.optimizedcv.state.OptimizedCvUiState
import com.iti.careerpilot.ats.presentation.optimizedcv.view.OptimizedCvScreen
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringIntent
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringUiState
import com.iti.careerpilot.ats.presentation.scoring.view.ScoringScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlinx.collections.immutable.persistentListOf

class AtsScreenStateTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun synchronizedCvAndValidUrlEnableCompare() {
        var intent: AtsEntryIntent? = null
        composeRule.setContent {
            MaterialTheme {
                AtsEntryScreen(
                    stateProvider = { AtsEntryUiState(
                        jobUrl = "https://example.com/jobs/1",
                        isUrlValid = true,
                        cvFileName = "resume.pdf",
                        cvSizeBytes = 2_048,
                        hasSynchronizedCv = true,
                    ) },
                    onIntent = { intent = it },
                )
            }
        }

        composeRule.onNodeWithText("resume.pdf").assertExists()
        composeRule.onNodeWithText("Compare Now").assertIsEnabled().performClick()
        composeRule.runOnIdle { assertEquals(AtsEntryIntent.CompareClicked, intent) }
    }

    @Test
    fun missingCvDisablesCompare() {
        composeRule.setContent {
            MaterialTheme {
                AtsEntryScreen(
                    stateProvider = { AtsEntryUiState(
                        jobUrl = "https://example.com/jobs/1",
                        isUrlValid = true,
                    ) },
                    onIntent = {},
                )
            }
        }

        composeRule.onNodeWithText("Tap to upload your CV").assertExists()
        composeRule.onNodeWithText("Compare Now").assertIsNotEnabled()
    }

    @Test
    fun scoringRendersReturnedSkillAndNeutralScoreWording() {
        composeRule.setContent {
            MaterialTheme {
                ScoringScreen(
                    stateProvider = { ScoringUiState(
                        isLoading = false,
                        workspace = workspace(),
                        score = AtsScore(
                            overallScore = 78,
                            matchPercentage = 78,
                            matchedSkills = persistentListOf("Kotlin"),
                            missingRequiredSkills = persistentListOf("Docker"),
                            missingPreferredSkills = persistentListOf("Terraform"),
                            strengths = persistentListOf("Strong architecture"),
                            weaknesses = persistentListOf("Add delivery metrics"),
                            sections = persistentListOf(AtsSectionScore("Projects", 59, "Add impact.")),
                            recommendations = persistentListOf("Quantify project impact"),
                            coinCost = null,
                            cvScoreUpdatedAt = null,
                        ),
                        trackId = null,
                    ) },
                    onIntent = {},
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText("MATCH SCORE").assertExists()
        composeRule.onNodeWithText("Kotlin").assertExists()
        composeRule.onNodeWithText("Good Match").assertExists()
        composeRule.onNodeWithText("✓ 1 matched").assertExists()
        composeRule.onNodeWithText("× 2 missing").assertExists()
        composeRule.onNodeWithText("Docker").assertExists()
        composeRule.onNodeWithText("Terraform").assertExists()
        composeRule.onNodeWithText("Strong architecture").assertExists()
        composeRule.onNodeWithText("Quantify project impact").assertExists()
        composeRule.onNodeWithText("Back").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Back").assertExists()
        composeRule.onNodeWithContentDescription("Expand section").assertExists()
        composeRule.onNodeWithText("Projects").performClick()
        composeRule.onNodeWithText("Add impact.").assertExists()
        composeRule.onNodeWithContentDescription("Collapse section").assertExists()
    }

    @Test
    fun importedWorkspaceRendersJobDetailsAndStartsScoring() {
        var intent: JobDetailsIntent? = null
        composeRule.setContent {
            MaterialTheme {
                JobDetailsScreen(
                    stateProvider = { JobDetailsUiState(
                        isLoading = false,
                        workspace = workspace(),
                    ) },
                    onIntent = { intent = it },
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText("Job Description").assertExists()
        composeRule.onNodeWithText("Senior Android Engineer").assertExists()
        composeRule.onNodeWithText("Build accessible Android products.").assertExists()
        composeRule.onNodeWithText("Kotlin").assertExists()
        composeRule.onNodeWithText("Start Scoring").performClick()
        composeRule.runOnIdle { assertEquals(JobDetailsIntent.StartScoring, intent) }
    }

    @Test
    fun coverLetterEditStateUsesCurrentValueAndAvailableContacts() {
        composeRule.setContent {
            MaterialTheme {
                CoverLetterScreen(
                    stateProvider = { CoverLetterUiState(
                        generatedValue = "Generated letter",
                        editedValue = "Edited letter",
                        contactName = "Sarah Chen",
                        contactEmail = "sarah@example.com",
                        isLoading = false,
                        isEditing = true,
                    ) },
                    onIntent = { _: CoverLetterIntent -> },
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText("Edited letter").assertTextContains("Edited letter")
        composeRule.onNodeWithText("Sarah Chen").assertExists()
        composeRule.onNodeWithText("sarah@example.com").assertExists()
    }

    @Test
    fun optimizedCvExpandsSectionImprovementsAndHandlesGoodEnoughSection() {
        composeRule.setContent {
            MaterialTheme {
                OptimizedCvScreen(
                    stateProvider = { OptimizedCvUiState(
                        sections = persistentListOf(
                            CvOptimizationSection(
                                name = "Experience",
                                score = 82,
                                improvements = persistentListOf(
                                    CvSectionImprovement(
                                        original = "Worked on APIs.",
                                        improved = "Delivered 12 APIs.",
                                        reason = "Adds measurable impact.",
                                    ),
                                ),
                            ),
                            CvOptimizationSection(
                                name = "Skills",
                                score = 90,
                                improvements = persistentListOf(),
                            ),
                        ),
                        recommendedTracks = persistentListOf(),
                        isLoading = false,
                    ) },
                    onIntent = {},
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText("Experience").performClick()
        composeRule.onNodeWithText("Worked on APIs.").assertExists()
        composeRule.onNodeWithText("Delivered 12 APIs.").assertExists()
        composeRule.onNodeWithText("Adds measurable impact.").assertExists()
        composeRule.onNodeWithText("Skills").performClick()
        composeRule.onNodeWithText(
            "This section is already strong enough. No improvements are needed.",
        ).assertExists()
    }

    private fun workspace() = JobWorkspace(
        id = 7,
        job = JobListing(
            id = 11,
            title = "Senior Android Engineer",
            companyName = "CareerPilot",
            location = "Cairo",
            description = "Build accessible Android products.",
            employmentType = null,
            seniorityLevel = null,
            requiredSkills = persistentListOf("Kotlin"),
            preferredSkills = persistentListOf(),
            responsibilities = persistentListOf(),
            qualifications = persistentListOf(),
            technologies = persistentListOf(),
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
