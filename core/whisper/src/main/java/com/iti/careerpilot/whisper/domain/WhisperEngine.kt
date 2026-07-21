package com.iti.careerpilot.whisper.domain


interface WhisperEngine {
    suspend fun transcribe(audioData: FloatArray): Result<String>
}
