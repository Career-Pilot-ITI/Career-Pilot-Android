package com.iti.careerpilot.home.presentation.home

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.iti.careerpilot.core.designsystem.CareerPilotTheme
import com.iti.careerpilot.home.presentation.home.screen.components.AtsJobMatchCard
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AtsJobMatchCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun cardShowsReferenceCopyAndHandlesClick() {
        var wasClicked = false

        composeRule.setContent {
            CareerPilotTheme {
                AtsJobMatchCard(onClick = { wasClicked = true })
            }
        }

        composeRule.onNodeWithText("ATS Job Match").assertExists().performClick()
        composeRule.onNodeWithText("NEW").assertExists()
        composeRule.onNodeWithText("Paste a job link · See how your CV scores").assertExists()
        composeRule.runOnIdle { assertTrue(wasClicked) }
    }

    @Test
    fun cardShowsLockedPlusBadgeWhenLocked() {
        var wasClicked = false

        composeRule.setContent {
            CareerPilotTheme {
                AtsJobMatchCard(
                    isLocked = true,
                    onClick = { wasClicked = true },
                )
            }
        }

        composeRule.onNodeWithText("ATS Job Match").assertExists().performClick()
        composeRule.onNodeWithText("PLUS").assertExists()
        composeRule.onNodeWithText("Paste a job link · See how your CV scores").assertExists()
        composeRule.runOnIdle { assertTrue(wasClicked) }
    }
}
