package com.iti.careerpilot.whisper.data

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
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

    override suspend fun transcribe(
        filePath: String,
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val decoded =
                decodeAudio(filePath) ?: throw IllegalStateException("Failed to decode audio")
            val mono =
                if (decoded.channelCount == 2) toMono(decoded.samples)
                else decoded.samples
            val finalAudioData =
                if (decoded.sampleRate != 16000) resampleTo16k(mono, decoded.sampleRate)
                else mono

            val engine = getRecognizer()
            val stream = engine.createStream()
            stream.acceptWaveform(finalAudioData, 16000)
            engine.decode(stream)
            val result = engine.getResult(stream)
            result.text.trim()
        }
            .onFailure {
                Log.e("CareerPilot", "transcribe: Failed", it)
            }
    }

    private fun decodeAudio(filePath: String): DecodedAudio? {
        val extractor = MediaExtractor()
        var codec: MediaCodec? = null
        try {
            extractor.setDataSource(filePath)
            val trackIndex = (0 until extractor.trackCount).firstOrNull {
                extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)
                    ?.startsWith("audio/") == true
            } ?: return null

            extractor.selectTrack(trackIndex)
            val format = extractor.getTrackFormat(trackIndex)
            val actualSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val actualChannelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

            codec = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!).apply {
                configure(format, null, null, 0)
                start()
            }

            val info = MediaCodec.BufferInfo()
            var buffer = FloatArray(1 shl 16)
            var count = 0
            fun append(samples: ShortArray) {
                if (count + samples.size > buffer.size) {
                    buffer = buffer.copyOf(maxOf(buffer.size * 2, count + samples.size))
                }
                for (s in samples) buffer[count++] = s / 32768f
            }

            var isEOS = false
            while (!isEOS) {
                val inputIndex = codec.dequeueInputBuffer(10_000)
                if (inputIndex >= 0) {
                    val inputBuffer = codec.getInputBuffer(inputIndex)!!
                    val sampleSize = extractor.readSampleData(inputBuffer, 0)
                    if (sampleSize < 0) {
                        codec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        isEOS = true
                    } else {
                        codec.queueInputBuffer(inputIndex, 0, sampleSize, extractor.sampleTime, 0)
                        extractor.advance()
                    }
                }

                var outputIndex = codec.dequeueOutputBuffer(info, 10_000)
                while (outputIndex >= 0) {
                    val outputBuffer = codec.getOutputBuffer(outputIndex)!!
                    val samples = ShortArray(info.size / 2)
                    outputBuffer.asShortBuffer().get(samples)
                    append(samples)
                    codec.releaseOutputBuffer(outputIndex, false)
                    outputIndex = codec.dequeueOutputBuffer(info, 10_000)
                }
            }

            return DecodedAudio(buffer.copyOf(count), actualSampleRate, actualChannelCount)
        } catch (e: Exception) {
            Log.e("PracticeSessionVM", "Failed to decode audio at $filePath", e)
            return null
        } finally {
            codec?.stop()
            codec?.release()
            extractor.release()
        }
    }

    private fun toMono(interleaved: FloatArray): FloatArray {
        val out = FloatArray(interleaved.size / 2)
        for (i in out.indices) {
            out[i] = (interleaved[i * 2] + interleaved[i * 2 + 1]) / 2f
        }
        return out
    }

    fun resampleTo16k(input: FloatArray, inputSampleRate: Int): FloatArray {
        if (inputSampleRate == 16000) return input
        val ratio = 16000.0 / inputSampleRate
        val outputLength = (input.size * ratio).toInt()
        val output = FloatArray(outputLength)
        for (i in output.indices) {
            val srcPos = i / ratio
            val srcIndex = srcPos.toInt()
            val frac = srcPos - srcIndex
            val a = input.getOrElse(srcIndex) { 0f }
            val b = input.getOrElse(srcIndex + 1) { a }
            output[i] = a + (b - a) * frac.toFloat()
        }
        return output
    }
}

class DecodedAudio(
    val samples: FloatArray,
    val sampleRate: Int,
    val channelCount: Int
)

