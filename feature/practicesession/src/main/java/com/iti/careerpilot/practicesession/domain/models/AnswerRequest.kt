package com.iti.careerpilot.practicesession.domain.models

data class AnswerRequest(
    val transcript: String,
    val sessionElapsedSeconds: Int,
    val durationMs: Int,
    val audioUrl: String,
    val words: List<Word>
)

data class Word(
    val word: String,
    val startMs: Int,
    val endMs: Int
)
