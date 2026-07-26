package com.iti.careerpilot.whisper.domain


interface WhisperEngine {
    suspend fun transcribe(filePath: String,): Result<String>
}
