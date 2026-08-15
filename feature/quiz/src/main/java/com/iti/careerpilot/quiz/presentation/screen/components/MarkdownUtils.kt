package com.iti.careerpilot.quiz.presentation.screen.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.iti.careerpilot.core.designsystem.CareerPilotTheme

@Composable
fun parseMarkdown(
    text: String,
    boldColor: Color = MaterialTheme.colorScheme.secondary,
    italicColor: Color = MaterialTheme.colorScheme.primary,
    inlineCodeColor: Color = CareerPilotTheme.extendedColors.studioTextPrimary,
    inlineCodeBackgroundColor: Color = CareerPilotTheme.extendedColors.studioSurfaceElevated2
): AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0
        val regex = Regex("""(\*\*.*?\*\*|\*.*?\*|`.*?`)""")
        val matches = regex.findAll(text)

        matches.forEach { match ->
            val start = match.range.first
            val end = match.range.last + 1
            val value = match.value

            // Append text before match
            append(text.substring(currentIndex, start))

            when {
                value.startsWith("**") && value.endsWith("**") -> {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = boldColor
                        )
                    ) {
                        append(value.substring(2, value.length - 2))
                    }
                }
                value.startsWith("*") && value.endsWith("*") -> {
                    withStyle(
                        SpanStyle(
                            fontStyle = FontStyle.Italic,
                            color = italicColor
                        )
                    ) {
                        append(value.substring(1, value.length - 1))
                    }
                }
                value.startsWith("`") && value.endsWith("`") -> {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            color = inlineCodeColor,
                            background = inlineCodeBackgroundColor
                        )
                    ) {
                        append(value.substring(1, value.length - 1))
                    }
                }
            }
            currentIndex = end
        }
        append(text.substring(currentIndex))
    }
}
