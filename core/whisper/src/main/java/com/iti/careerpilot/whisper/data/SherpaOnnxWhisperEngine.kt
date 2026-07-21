package com.iti.careerpilot.whisper.data

import android.content.Context
import android.util.Log
import com.iti.careerpilot.whisper.domain.WhisperEngine
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 1. Download the models from "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-whisper-tiny.en.tar.bz2"
 * 2. Extract and copy 3 files
 *   encoder "tiny.en-encoder.int8.onnx"
 *   decoder "tiny.en-decoder.int8.onnx"
 *   tokens  "tiny.en-tokens.txt"
 *   and move them to "src/main/assets/models" folder
 */
@Singleton
class SherpaOnnxWhisperEngine @Inject constructor(
    @param:ApplicationContext private val context: Context
) : WhisperEngine {

    private var recognizer: OfflineRecognizer? = null
    private val mutex = Mutex()

    private suspend fun getRecognizer(): OfflineRecognizer = mutex.withLock {
        recognizer ?: createRecognizer().also { recognizer = it }
    }

    private fun createRecognizer(): OfflineRecognizer {
        val config = OfflineRecognizerConfig(
            modelConfig = OfflineModelConfig(
                whisper = OfflineWhisperModelConfig(
                    encoder = "models/tiny.en-encoder.int8.onnx",
                    decoder = "models/tiny.en-decoder.int8.onnx",
                    language = "en",
                    task = "transcribe"
                ),
                tokens = "models/tiny.en-tokens.txt",
                modelType = "whisper",
                numThreads = 1,
                debug = false
            )
        )
        return OfflineRecognizer(context.assets, config)
    }

    override suspend fun transcribe(audioData: FloatArray): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val engine = getRecognizer()
            val stream = engine.createStream()
            stream.acceptWaveform(audioData, 16000)
            engine.decode(stream)
            val result = engine.getResult(stream)
            result.text.trim()
        }
            .onFailure {
                Log.e("CareerPilot", "transcribe: Failed", it)
            }
    }
}
