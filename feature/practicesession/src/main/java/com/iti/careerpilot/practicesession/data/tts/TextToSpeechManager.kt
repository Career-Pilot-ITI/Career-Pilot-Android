package com.iti.careerpilot.practicesession.data.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class TextToSpeechManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : TextToSpeech.OnInitListener, AutoCloseable {

    private var textToSpeech: TextToSpeech? = null

    @Volatile
    private var isInitialized = false

    init {
        try {
            textToSpeech = TextToSpeech(context, this)
        } catch (_: Throwable) {
            // Stub on JVM unit test framework
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.apply {
                language = Locale.US
                setSpeechRate(1f)
                setPitch(1f)
            }
            isInitialized = true
        }
    }

    fun setOnUtteranceProgressListener(listener: UtteranceProgressListener) {
        textToSpeech?.setOnUtteranceProgressListener(listener)
    }

    fun speak(
        text: String,
        flushQueue: Boolean = true,
        utteranceId: String = UUID.randomUUID().toString(),
    ) {
        if (!isInitialized || text.isBlank()) return

        textToSpeech?.speak(
            text,
            if (flushQueue) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD,
            null,
            utteranceId,
        )
    }

    fun stop() {
        textToSpeech?.stop()
    }

    fun isSpeaking(): Boolean {
        return textToSpeech?.isSpeaking == true
    }

    fun setSpeechRate(rate: Float) {
        textToSpeech?.setSpeechRate(rate)
    }

    fun setPitch(pitch: Float) {
        textToSpeech?.setPitch(pitch)
    }

    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
    }

    override fun close() {
        shutdown()
    }
}