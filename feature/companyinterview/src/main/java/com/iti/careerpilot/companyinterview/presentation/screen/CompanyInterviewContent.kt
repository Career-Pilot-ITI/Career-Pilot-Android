package com.iti.careerpilot.companyinterview.presentation.screen

import androidx.camera.core.ImageProxy
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iti.careerpilot.companyinterview.presentation.mvi.CompanyInterviewStep
import com.iti.careerpilot.companyinterview.presentation.mvi.CompanyInterviewUiIntent
import com.iti.careerpilot.companyinterview.presentation.mvi.CompanyInterviewUiState
import com.iti.careerpilot.companyinterview.presentation.screen.components.*

@Composable
fun CompanyInterviewContent(
    state: CompanyInterviewUiState,
    onIntent: (CompanyInterviewUiIntent) -> Unit,
    onFrame: (ImageProxy) -> Unit,
    modifier: Modifier = Modifier
) {
    val brandColor = remember(state.metadata?.companyBrandColor) {
        val hex = state.metadata?.companyBrandColor ?: "#2563EB"
        runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color(0xFF2563EB))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val step = state.step) {
            is CompanyInterviewStep.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = brandColor)
                }
            }

            is CompanyInterviewStep.EmailVerificationRequired -> {
                EmailVerificationDialog(
                    expectedDomainOrHint = state.metadata?.applicantEmail ?: "",
                    onVerify = { onIntent(CompanyInterviewUiIntent.VerifyEmail(it)) },
                    onDismiss = { onIntent(CompanyInterviewUiIntent.ExitInterview) }
                )
            }

            is CompanyInterviewStep.MetadataOverview -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Company Logo / Initials
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = brandColor.copy(alpha = 0.12f),
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = state.metadata?.companyName?.take(2)?.uppercase() ?: "CP",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = brandColor
                                )
                            )
                        }
                    }

                    Text(
                        text = state.metadata?.title ?: "Interview Assessment",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Invited by ${state.metadata?.companyName ?: "Hiring Team"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Information Card
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Assessment Structure",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = brandColor, modifier = Modifier.size(20.dp))
                                Text(
                                    text = "${state.metadata?.totalQuestions ?: 0} Timed Questions",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = brandColor, modifier = Modifier.size(20.dp))
                                Text(
                                    text = "Voice answers recorded & transcribed",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Videocam, contentDescription = null, tint = brandColor, modifier = Modifier.size(20.dp))
                                Text(
                                    text = "Camera active for presence & eye contact",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = brandColor, modifier = Modifier.size(20.dp))
                                Text(
                                    text = "Single attempt: cannot be paused or restarted",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { onIntent(CompanyInterviewUiIntent.ConfirmRulesAndStart) },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = brandColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = "Begin Interview Now",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            is CompanyInterviewStep.ActiveInterview -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    CompanyInterviewTopBar(
                        companyName = state.metadata?.companyName ?: "Company Interview",
                        currentQuestionIndex = state.currentQuestionIndex,
                        totalQuestions = state.totalQuestions,
                        brandColor = brandColor,
                        onCloseClicked = { onIntent(CompanyInterviewUiIntent.ExitInterview) }
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Countdown Timer
                        QuestionTimerCountdown(
                            remainingSeconds = step.remainingSeconds,
                            totalSeconds = step.question.timeLimitSeconds
                        )

                        // Question Text Card
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = brandColor.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "Question ${step.question.questionOrder} of ${step.question.totalQuestions}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = brandColor
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                Text(
                                    text = step.question.questionText,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center,
                                        fontSize = 19.sp,
                                        lineHeight = 26.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Camera Proctoring PIP + Visual Indicator
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CameraProctoringPreview(
                                onFrame = onFrame,
                                isRecording = step.isRecording,
                                eyeContactScore = step.eyeContactScore
                            )
                        }

                        // Bottom Actions: Submit Answer Button
                        Button(
                            onClick = { onIntent(CompanyInterviewUiIntent.StopAndSubmitAnswer) },
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = brandColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Text(
                                    text = if (step.question.isLastQuestion) "Finish & Submit Assessment" else "Submit Answer & Next",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            is CompanyInterviewStep.SubmittingAnswer -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = brandColor)
                        Text(
                            text = "Transcribing & Evaluating...",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            is CompanyInterviewStep.CompletedSuccess -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF22C55E).copy(alpha = 0.15f),
                        modifier = Modifier.size(96.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Done",
                                tint = Color(0xFF22C55E),
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Interview Submitted!",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Thank you for completing your assessment for ${state.metadata?.companyName ?: "the hiring team"}. Your responses have been submitted for review. You may now close the application.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { onIntent(CompanyInterviewUiIntent.ExitInterview) },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = brandColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = "Return to Home",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            is CompanyInterviewStep.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Something went wrong",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = step.message,
                        style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { onIntent(CompanyInterviewUiIntent.ExitInterview) },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Exit Interview")
                    }
                }
            }

            else -> {}
        }
    }
}
