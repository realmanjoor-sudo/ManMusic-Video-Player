package com.manmusic.videoplayer.data.media

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VolumeState(
    val currentVolume: Int = 10,
    val maxVolume: Int = 15,
    val volumeRatio: Float = 0.66f,
    val isMuted: Boolean = false,
    val percentage: Int = 66,
    val isExpanded: Boolean = false
)

/**
 * Controller strictly dedicated to AudioManager.STREAM_MUSIC.
 * Ensures ringtone (STREAM_RING) and alarm (STREAM_ALARM) volume streams are never touched.
 * Synchronizes with hardware volume keys and accessibility thresholds.
 */
class VolumeController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var lastNonZeroVolume: Int = 10

    private val _volumeState = MutableStateFlow(computeCurrentState())
    val volumeState: StateFlow<VolumeState> = _volumeState.asStateFlow()

    private val volumeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "android.media.VOLUME_CHANGED_ACTION") {
                val streamType = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1)
                if (streamType == AudioManager.STREAM_MUSIC) {
                    _volumeState.value = computeCurrentState()
                }
            }
        }
    }

    init {
        try {
            val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
            context.registerReceiver(volumeReceiver, filter)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun computeCurrentState(): VolumeState {
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val ratio = current.toFloat() / max
        val percentage = (ratio * 100).toInt()
        val isMuted = current == 0

        if (current > 0) {
            lastNonZeroVolume = current
        }

        return VolumeState(
            currentVolume = current,
            maxVolume = max,
            volumeRatio = ratio,
            isMuted = isMuted,
            percentage = percentage,
            isExpanded = _volumeState.value.isExpanded
        )
    }

    fun setVolumeRatio(ratio: Float) {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val targetVolume = (ratio.coerceIn(0f, 1f) * max).toInt()
        setVolumeLevel(targetVolume)
    }

    fun setVolumeLevel(level: Int) {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val bounded = level.coerceIn(0, max)
        try {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, bounded, 0)
            _volumeState.value = computeCurrentState()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleMute() {
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        if (current > 0) {
            lastNonZeroVolume = current
            setVolumeLevel(0)
        } else {
            setVolumeLevel(if (lastNonZeroVolume > 0) lastNonZeroVolume else 5)
        }
    }

    fun setExpanded(expanded: Boolean) {
        _volumeState.value = _volumeState.value.copy(isExpanded = expanded)
    }

    fun unregister() {
        try {
            context.unregisterReceiver(volumeReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}