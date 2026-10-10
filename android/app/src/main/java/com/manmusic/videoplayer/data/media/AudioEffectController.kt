package com.manmusic.videoplayer.data.media

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log

data class EqualizerBandInfo(
    val index: Int,
    val centerFreqHz: Int,
    val minLevelMilliBel: Int,
    val maxLevelMilliBel: Int,
    var currentLevelMilliBel: Int
) {
    val centerFreqFormatted: String
        get() = if (centerFreqHz >= 1000) "${centerFreqHz / 1000} kHz" else "$centerFreqHz Hz"

    val levelDbFormatted: String
        get() {
            val db = currentLevelMilliBel / 100
            return if (db > 0) "+${db} dB" else "${db} dB"
        }
}

data class AudioEffectsState(
    val isEnabled: Boolean = true,
    val isAvailableOnDevice: Boolean = true,
    val selectedPreset: String = "Normal",
    val availablePresets: List<String> = listOf("Normal", "Rock", "Pop", "Classical", "Jazz", "Dance"),
    val bands: List<EqualizerBandInfo> = emptyList(),
    val bassBoostStrength: Int = 0,
    val isBassBoostSupported: Boolean = true,
    val virtualizerStrength: Int = 0,
    val isVirtualizerSupported: Boolean = true,
    val statusMessage: String = "Active on Audio Session"
)

/**
 * Controller for Android hardware audio effects attached to active audio sessions.
 * Manages Equalizer, BassBoost, and Virtualizer lifecycle without memory leaks or duplicate instances.
 */
class AudioEffectController(private val context: Context) {

    private var currentAudioSessionId: Int = 0
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    var currentState: AudioEffectsState = AudioEffectsState()
        private set

    fun attachToAudioSession(audioSessionId: Int): AudioEffectsState {
        if (audioSessionId == 0) {
            return currentState.copy(
                isAvailableOnDevice = false,
                statusMessage = "Waiting for valid audio session"
            )
        }

        if (currentAudioSessionId == audioSessionId && equalizer != null) {
            return currentState
        }

        releaseEffects()
        currentAudioSessionId = audioSessionId

        try {
            val eq = Equalizer(0, audioSessionId)
            equalizer = eq
            eq.enabled = currentState.isEnabled

            val numBands = eq.numberOfBands.toInt()
            val minLevel = eq.bandLevelRange[0].toInt()
            val maxLevel = eq.bandLevelRange[1].toInt()

            val bandList = mutableListOf<EqualizerBandInfo>()
            for (i in 0 until numBands) {
                val centerFreq = eq.getCenterFreq(i.toShort()) / 1000
                val level = eq.getBandLevel(i.toShort()).toInt()
                bandList.add(
                    EqualizerBandInfo(
                        index = i,
                        centerFreqHz = centerFreq,
                        minLevelMilliBel = minLevel,
                        maxLevelMilliBel = maxLevel,
                        currentLevelMilliBel = level
                    )
                )
            }

            var bassSupported = false
            try {
                val bb = BassBoost(0, audioSessionId)
                bassBoost = bb
                bassSupported = bb.strengthSupported
                if (bassSupported) {
                    bb.enabled = currentState.isEnabled
                    bb.setStrength(currentState.bassBoostStrength.toShort())
                }
            } catch (e: Exception) {
                Log.w("AudioEffectController", "BassBoost not supported: ${e.message}")
            }

            var virtSupported = false
            try {
                val virt = Virtualizer(0, audioSessionId)
                virtualizer = virt
                virtSupported = virt.strengthSupported
                if (virtSupported) {
                    virt.enabled = currentState.isEnabled
                    virt.setStrength(currentState.virtualizerStrength.toShort())
                }
            } catch (e: Exception) {
                Log.w("AudioEffectController", "Virtualizer not supported: ${e.message}")
            }

            currentState = currentState.copy(
                isAvailableOnDevice = true,
                bands = bandList,
                isBassBoostSupported = bassSupported,
                isVirtualizerSupported = virtSupported,
                statusMessage = "Hardware Equalizer active on session $audioSessionId"
            )
        } catch (e: Exception) {
            Log.e("AudioEffectController", "Audio effects initialization failed: ${e.message}")
            currentState = currentState.copy(
                isAvailableOnDevice = false,
                statusMessage = "Audio effects not supported on this device/session"
            )
        }

        return currentState
    }

    fun setEnabled(enabled: Boolean): AudioEffectsState {
        currentState = currentState.copy(isEnabled = enabled)
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
            virtualizer?.enabled = enabled
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return currentState
    }

    fun setBandLevel(bandIndex: Int, levelMilliBel: Int): AudioEffectsState {
        try {
            equalizer?.setBandLevel(bandIndex.toShort(), levelMilliBel.toShort())
            val updatedBands = currentState.bands.map {
                if (it.index == bandIndex) it.copy(currentLevelMilliBel = levelMilliBel) else it
            }
            currentState = currentState.copy(bands = updatedBands, selectedPreset = "Custom")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return currentState
    }

    fun applyPreset(presetName: String): AudioEffectsState {
        val bands = currentState.bands
        if (bands.isEmpty()) return currentState

        val curveMultipliers = when (presetName.lowercase()) {
            "rock" -> listOf(0.4f, 0.2f, -0.1f, 0.3f, 0.5f)
            "pop" -> listOf(-0.1f, 0.2f, 0.4f, 0.1f, -0.1f)
            "classical" -> listOf(0.3f, 0.2f, -0.1f, 0.2f, 0.3f)
            "jazz" -> listOf(0.2f, 0.1f, 0.1f, 0.2f, 0.3f)
            "dance" -> listOf(0.6f, 0.4f, 0.1f, 0.3f, 0.2f)
            "normal", "flat" -> listOf(0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
            else -> listOf(0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
        }

        val updatedBands = bands.mapIndexed { idx, band ->
            val factor = curveMultipliers.getOrElse(idx) { 0.0f }
            val level = (band.maxLevelMilliBel * factor).toInt()
            try {
                equalizer?.setBandLevel(band.index.toShort(), level.toShort())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            band.copy(currentLevelMilliBel = level)
        }

        currentState = currentState.copy(bands = updatedBands, selectedPreset = presetName)
        return currentState
    }

    fun setBassBoost(strength: Int): AudioEffectsState {
        val bounded = strength.coerceIn(0, 1000)
        try {
            bassBoost?.setStrength(bounded.toShort())
            currentState = currentState.copy(bassBoostStrength = bounded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return currentState
    }

    fun setVirtualizer(strength: Int): AudioEffectsState {
        val bounded = strength.coerceIn(0, 1000)
        try {
            virtualizer?.setStrength(bounded.toShort())
            currentState = currentState.copy(virtualizerStrength = bounded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return currentState
    }

    fun reset(): AudioEffectsState {
        return applyPreset("Normal").also {
            setBassBoost(0)
            setVirtualizer(0)
        }
    }

    fun releaseEffects() {
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        equalizer = null
        bassBoost = null
        virtualizer = null
        currentAudioSessionId = 0
    }
}