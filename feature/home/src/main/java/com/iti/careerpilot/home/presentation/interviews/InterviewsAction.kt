package com.iti.careerpilot.home.presentation.interviews

sealed interface InterviewsAction {
    data class QueryChanged(val query: String) : InterviewsAction
    data class PracticeTrackClicked(val trackId: Long, val trackName: String) : InterviewsAction
    data class LessonTrackClicked(val trackId: Long, val trackName: String) : InterviewsAction
    data object Retry : InterviewsAction
}