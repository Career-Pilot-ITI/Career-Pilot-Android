package com.iti.careerpilot.home.presentation.interviews

sealed interface InterviewsIntent {
    data object Initial : InterviewsIntent
    data class QueryChanged(val query: String) : InterviewsIntent
    data class PracticeTrackClicked(val trackId: Long, val trackName: String) : InterviewsIntent
    data class LessonTrackClicked(val trackId: Long, val trackName: String) : InterviewsIntent
    data object Retry : InterviewsIntent
}
