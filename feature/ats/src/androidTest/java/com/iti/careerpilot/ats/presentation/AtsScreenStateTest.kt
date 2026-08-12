package com.iti.careerpilot.ats.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.iti.careerpilot.ats.domain.model.AtsScore
import com.iti.careerpilot.ats.domain.model.AtsSectionScore
import com.iti.careerpilot.ats.domain.model.JobListing
import com.iti.careerpilot.ats.domain.model.JobWorkspace
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterAction
import com.iti.careerpilot.ats.presentation.coverletter.state.CoverLetterUiState
import com.iti.careerpilot.ats.presentation.coverletter.view.CoverLetterScreen
import com.iti.careerpilot.ats.presentation.entry.state.AtsEntryAction
import com.iti.careerpilot.ats.presentation.entry.view.AtsEntryScreen
import com.iti.careerpilot.ats.presentation.entry.state.AtsEntryUiState
import com.iti.careerpilot.ats.presentation.optimizedcv.state.OptimizedCvUiState
import com.iti.careerpilot.ats.presentation.optimizedcv.view.OptimizedCvScreen
import com.iti.careerpilot.ats.presentation.scoring.state.ScoringUiState
import com.iti.careerpilot.ats.presentation.scoring.view.ScoringScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AtsScreenStateTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun synchronizedCvAndValidUrlEnableCompare() {
        var action: AtsEntryAction? = null
        composeRule.setContent {
            MaterialTheme {
                AtsEntryScreen(
                    state = AtsEntryUiState(
                        jobUrl = "https://example.com/jobs/1",
                        isUrlValid = true,
                        cvFileName = "resume.pdf",
                        cvSizeBytes = 2_048,
                        hasSynchronizedCv = true,
                    ),
                    onAction = { action = it },
                )
            }
        }

        composeRule.onNodeWithText("resume.pdf").assertExists()
        composeRule.onNodeWithText("Compare Now").assertIsEnabled().performClick()
        composeRule.runOnIdle { assertEquals(AtsEntryAction.CompareClicked, action) }
    }

    @Test
    fun missingCvDisablesCompare() {
        composeRule.setContent {
            MaterialTheme {
                AtsEntryScreen(
                    state = AtsEntryUiState(
                        jobUrl = "https://example.com/jobs/1",
                        isUrlValid = true,
                    ),
                    onAction = {},
                )
            }
        }

        composeRule.onNodeWithText("Upload your CV").assertExists()
        composeRule.onNodeWithText("Compare Now").assertIsNotEnabled()
    }

    @Test
    fun scoringRendersReturnedSkillAndNeutralScoreWording() {
        composeRule.setContent {
            MaterialTheme {
                ScoringScreen(
                    state = ScoringUiState(
                        isLoading = false,
                        workspace = workspace(),
                        score = AtsScore(
                            overallScore = 78,
                            matchPercentage = 78,
                            matchedSkills = listOf("Kotlin"),
                            missingRequiredSkills = emptyList(),
                            missingPreferredSkills = emptyList(),
                            strengths = emptyList(),
                            weaknesses = emptyList(),
                            sections = listOf(AtsSectionScore("Projects", 59, "Add impact.")),
                            recommendations = emptyList(),
                            coinCost = null,
                            cvScoreUpdatedAt = null,
                        ),
                        trackId = null,
                    ),
                    onAction = {},
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText("MATCH SCORE").assertExists()
        composeRule.onNodeWithText("Kotlin").assertExists()
        composeRule.onNodeWithText("Good Match").assertDoesNotExist()
    }

    @Test
    fun coverLetterEditStateUsesCurrentValueAndAvailableContacts() {
        composeRule.setContent {
            MaterialTheme {
                CoverLetterScreen(
                    state = CoverLetterUiState(
                        generatedValue = "Generated letter",
                        editedValue = "Edited letter",
                        contactName = "Sarah Chen",
                        contactEmail = "sarah@example.com",
                        isLoading = false,
                        isEditing = true,
                    ),
                    onAction = { _: CoverLetterAction -> },
                    onBack = {},
                    snackbarHostState = SnackbarHostState(),
                )
            }
        }

        composeRule.onNodeWithText("Edited letter").assertTextContains("Edited letter")
        composeRule.onNodeWithText("Sarah Chen").assertExists()
        composeRule.onNodeWithText("sarah@example.com").assertExists()
    }

    @Test
    fun optimizedCvShowsReturnedTextWithoutPdfAction() {
        composeRule.setContent {
            MaterialTheme {
                OptimizedCvScreen(
                    state = OptimizedCvUiState(
                        optimizedText = "Optimized experience text",
                        recommendedTracks = emptyList(),
                        isLoading = false,
                    ),
                    onAction = {},
                    onBack = {},
                    snackbarHostState = SnackbarHostState(),
                )
            }
        }

        composeRule.onNodeWithText("Optimized experience text").assertExists()
        composeRule.onNodeWithText("Download PDF").assertDoesNotExist()
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
            requiredSkills = listOf("Kotlin"),
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
