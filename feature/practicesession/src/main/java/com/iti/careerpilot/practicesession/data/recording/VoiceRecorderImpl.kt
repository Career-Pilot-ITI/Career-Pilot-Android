package com.iti.careerpilot.practicesession.data.recording

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.iti.careerpilot.practicesession.domain.recording.VoiceRecorder
import com.iti.careerpilot.practicesession.domain.recording.models.RecordingDetails
import com.iti.common.dispatcher.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds


class VoiceRecorderImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
) : VoiceRecorder {

    companion object {
        private const val MAX_AMPLITUDE_VALUE = 26_000L
        const val TEMP_DIRECTORY = "voice_recordings"
        const val RECORDING_FILE_EXTENSION = "mp4"
        const val TEMP_FILE_PREFIX = "temp_recording"
    }

    private val _recordingDetails = MutableStateFlow(RecordingDetails())
    override val recordingDetails = _recordingDetails.asStateFlow()

    private var tempFile = generateTempFile()

    private var recorder: MediaRecorder? = null
    private var isRecording: Boolean = false

    private var isPaused: Boolean = false

    private var durationJob: Job? = null
    private var amplitudeJob: Job? = null

    override fun start() {
        if (isRecording) {
            return
        }

        try {
            resetSession()

            tempFile = generateTempFile()

            recorder = newMediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128 * 1000)
                setAudioSamplingRate(44100)
                setOutputFile(tempFile.path)

                prepare()
                start()
            }

            isRecording = true
            isPaused = false

            startTrackingDuration()
            startTrackingAmplitudes()
        } catch (_: IOException) {
            recorder?.release()
            recorder = null
        }
    }

    private fun startTrackingAmplitudes() {
        amplitudeJob = applicationScope.launch {
            while (isRecording) {
                val amplitude = getAmplitude()
                _recordingDetails.update { currentDetails ->
                    currentDetails.copy(
                        amplitudes = currentDetails.amplitudes + amplitude,
                        isRecording = true,
                    )
                }
                delay(100L.milliseconds)
            }
        }
    }

    private fun getAmplitude(): Float {
        return if (isRecording) {
            try {
                val maxAmplitude = recorder?.maxAmplitude
                val amplitudeRatio = maxAmplitude?.takeIf { it > 0f }?.run {
                    (this / MAX_AMPLITUDE_VALUE.toFloat()).coerceIn(0f, 1f)
                }
                amplitudeRatio ?: 0f
            } catch (_: Exception) {
                0f
            }
        } else 0f
    }

    private fun startTrackingDuration() {
        durationJob = applicationScope.launch {
            var lastTime = System.currentTimeMillis()
            while (isRecording && !isPaused) {
                delay(10L.milliseconds)
                val currentTime = System.currentTimeMillis()
                val elapsedTime = currentTime - lastTime

                _recordingDetails.update {
                    it.copy(
                        duration = it.duration + elapsedTime.milliseconds
                    )
                }
                lastTime = System.currentTimeMillis()
            }
        }
    }

    private fun getTempDirectory(): File =
        File(context.cacheDir, TEMP_DIRECTORY).apply {
            if (!exists()) {
                mkdirs()
            }
        }

    private fun generateTempFile(): File {
        val id = UUID.randomUUID().toString()
        return File(
            getTempDirectory(),
            "${TEMP_FILE_PREFIX}_$id.$RECORDING_FILE_EXTENSION"
        )
    }

    private fun deleteAllTempFiles() {
        getTempDirectory().listFiles()?.forEach(File::delete)
    }

    private fun resetSession() {
        _recordingDetails.update { RecordingDetails() }
        applicationScope.launch {
            cleanup()
        }
    }

    private fun cleanup() {
        recorder?.release()
        recorder = null
        isRecording = false
        isPaused = false
        durationJob?.cancel()
        amplitudeJob?.cancel()
    }

    private fun newMediaRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
    }

    override fun pause() {
        if (!isRecording || isPaused) {
            return
        }
        _recordingDetails.update { it.copy(isRecording = false) }
        isPaused = true
        recorder?.pause()
        durationJob?.cancel()
        amplitudeJob?.cancel()
    }

    override fun stop() {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } finally {
            _recordingDetails.update {
                it.copy(
                    filePath = tempFile.path,
                    isRecording = false
                )
            }
            cleanup()
        }
    }

    override fun resume() {
        if (!isRecording || !isPaused) {
            return
        }
        recorder?.resume()
        isPaused = false
        startTrackingDuration()
        startTrackingAmplitudes()
    }

    override fun cancel() {
        stop()
        resetSession()
        deleteAllTempFiles()
    }
}