package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

object SoundCuePlayer {
    private const val SAMPLE_RATE = 22050
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _emergencyAlertSoundEnabled = MutableStateFlow(true)
    val emergencyAlertSoundEnabled: StateFlow<Boolean> = _emergencyAlertSoundEnabled.asStateFlow()

    fun toggleSound() {
        _soundEnabled.value = !_soundEnabled.value
    }

    fun setSoundEnabled(enabled: Boolean) {
        _soundEnabled.value = enabled
    }

    fun toggleEmergencyAlertSound() {
        _emergencyAlertSoundEnabled.value = !_emergencyAlertSoundEnabled.value
    }

    fun setEmergencyAlertSoundEnabled(enabled: Boolean) {
        _emergencyAlertSoundEnabled.value = enabled
    }

    fun playRoleRevealCue(isImpostor: Boolean) {
        if (!_soundEnabled.value) return
        val notes = if (isImpostor) {
            listOf(
                ToneSegment(freqHz = 220.0, durationMs = 130, gain = 0.22),
                ToneSegment(freqHz = 0.0, durationMs = 25, gain = 0.0),
                ToneSegment(freqHz = 185.0, durationMs = 220, gain = 0.24)
            )
        } else {
            listOf(
                ToneSegment(freqHz = 523.25, durationMs = 110, gain = 0.16),
                ToneSegment(freqHz = 0.0, durationMs = 18, gain = 0.0),
                ToneSegment(freqHz = 659.25, durationMs = 180, gain = 0.18)
            )
        }
        playSequence(notes)
    }

    fun playTimerExpiredCue() {
        if (!_soundEnabled.value) return
        playSequence(
            listOf(
                ToneSegment(freqHz = 783.99, durationMs = 95, gain = 0.18),
                ToneSegment(freqHz = 0.0, durationMs = 65, gain = 0.0),
                ToneSegment(freqHz = 659.25, durationMs = 150, gain = 0.20)
            )
        )
    }

    fun playEmergencyMeetingCue() {
        if (!_soundEnabled.value || !_emergencyAlertSoundEnabled.value) return
        playSequence(
            listOf(
                ToneSegment(freqHz = 880.0, durationMs = 110, gain = 0.22),
                ToneSegment(freqHz = 0.0, durationMs = 45, gain = 0.0),
                ToneSegment(freqHz = 622.25, durationMs = 130, gain = 0.24),
                ToneSegment(freqHz = 0.0, durationMs = 45, gain = 0.0),
                ToneSegment(freqHz = 880.0, durationMs = 180, gain = 0.24)
            )
        )
    }

    fun playVotingResultsCue(crewWon: Boolean) {
        if (!_soundEnabled.value) return
        val notes = if (crewWon) {
            listOf(
                ToneSegment(freqHz = 523.25, durationMs = 110, gain = 0.16),
                ToneSegment(freqHz = 659.25, durationMs = 110, gain = 0.17),
                ToneSegment(freqHz = 783.99, durationMs = 220, gain = 0.19)
            )
        } else {
            listOf(
                ToneSegment(freqHz = 392.0, durationMs = 120, gain = 0.18),
                ToneSegment(freqHz = 311.13, durationMs = 130, gain = 0.19),
                ToneSegment(freqHz = 261.63, durationMs = 240, gain = 0.20)
            )
        }
        playSequence(notes)
    }

    private fun playSequence(segments: List<ToneSegment>) {
        scope.launch {
            try {
                val pcm = synthesize(segments)
                if (pcm.isEmpty()) return@launch
                val minBufBytes = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(pcm.size * 2)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .setBufferSizeInBytes(maxOf(minBufBytes, pcm.size * 2))
                    .build()

                try {
                    track.write(pcm, 0, pcm.size)
                    track.play()
                    val totalMs = segments.sumOf { it.durationMs }.toLong() + 80L
                    kotlinx.coroutines.delay(totalMs)
                } finally {
                    try {
                        track.stop()
                    } catch (_: Throwable) {
                    }
                    track.release()
                }
            } catch (_: Throwable) {
                // Gracefully no-op on devices/test runners without audio sink
            }
        }
    }

    private fun synthesize(segments: List<ToneSegment>): ShortArray {
        val totalSamples = segments.sumOf { (SAMPLE_RATE * it.durationMs) / 1000 }
        val buffer = ShortArray(totalSamples)
        var cursor = 0
        for (seg in segments) {
            val count = (SAMPLE_RATE * seg.durationMs) / 1000
            if (count <= 0) continue
            val fadeSamples = (SAMPLE_RATE * 14) / 1000 // 14ms soft attack/release prevents clicks
            for (i in 0 until count) {
                if (seg.freqHz <= 0.0 || seg.gain <= 0.0) {
                    buffer[cursor++] = 0
                    continue
                }
                val attack = if (i < fadeSamples) i.toDouble() / fadeSamples else 1.0
                val release = if (i > count - fadeSamples) (count - i).toDouble() / fadeSamples else 1.0
                val env = minOf(attack, release).coerceIn(0.0, 1.0)
                val angle = 2.0 * PI * seg.freqHz * i / SAMPLE_RATE
                // Blend fundamental + gentle 2nd harmonic for warm chime character
                val wave = 0.85 * sin(angle) + 0.15 * sin(angle * 2.0)
                val sample = (wave * env * seg.gain * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    .toShort()
                buffer[cursor++] = sample
            }
        }
        return buffer
    }

    private data class ToneSegment(
        val freqHz: Double,
        val durationMs: Int,
        val gain: Double
    )
}
