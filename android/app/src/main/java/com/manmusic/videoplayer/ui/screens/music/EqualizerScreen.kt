package com.manmusic.videoplayer.ui.screens.music

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manmusic.videoplayer.data.media.AudioEffectsState
import com.manmusic.videoplayer.data.media.EqualizerBandInfo

@Composable
fun EqualizerDialog(
    effectsState: AudioEffectsState,
    onToggleEnable: (Boolean) -> Unit,
    onBandLevelChange: (Int, Int) -> Unit,
    onApplyPreset: (String) -> Unit,
    onBassBoostChange: (Int) -> Unit,
    onVirtualizerChange: (Int) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Audio Equalizer", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Switch(
                    checked = effectsState.isEnabled,
                    onCheckedChange = onToggleEnable,
                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (effectsState.isAvailableOnDevice)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    else
                        Color(0xFFF59E0B).copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (effectsState.isAvailableOnDevice) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (effectsState.isAvailableOnDevice) MaterialTheme.colorScheme.primary else Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = effectsState.statusMessage,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Column {
                    Text(
                        text = "PRESETS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(effectsState.availablePresets) { preset ->
                            val isSelected = effectsState.selectedPreset.equals(preset, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onApplyPreset(preset) },
                                label = { Text(preset, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "FREQUENCY BANDS (DEVICE-REPORTED LEVELS)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (effectsState.bands.isEmpty()) {
                        Text(
                            text = "Awaiting active audio playback to query hardware bands...",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    } else {
                        effectsState.bands.forEach { band ->
                            BandSliderRow(
                                band = band,
                                isEnabled = effectsState.isEnabled,
                                onLevelChange = { newLevel ->
                                    onBandLevelChange(band.index, newLevel)
                                }
                            )
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Speaker, contentDescription = null, tint = Color(0xFFF43F5E), modifier = Modifier.size(16.dp))
                                Text("Bass Boost", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "${(effectsState.bassBoostStrength / 10)}%",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFF43F5E)
                            )
                        }

                        Slider(
                            value = effectsState.bassBoostStrength.toFloat() / 1000f,
                            onValueChange = { onBassBoostChange((it * 1000).toInt()) },
                            enabled = effectsState.isEnabled && effectsState.isBassBoostSupported,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFF43F5E),
                                activeTrackColor = Color(0xFFF43F5E)
                            )
                        )
                    }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.SurroundSound, contentDescription = null, tint = Color(0xFF06B6D4), modifier = Modifier.size(16.dp))
                                Text("Virtualizer / Spatial Audio", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "${(effectsState.virtualizerStrength / 10)}%",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF06B6D4)
                            )
                        }

                        Slider(
                            value = effectsState.virtualizerStrength.toFloat() / 1000f,
                            onValueChange = { onVirtualizerChange((it * 1000).toInt()) },
                            enabled = effectsState.isEnabled && effectsState.isVirtualizerSupported,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF06B6D4),
                                activeTrackColor = Color(0xFF06B6D4)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Done") }
        },
        dismissButton = {
            TextButton(onClick = onReset) { Text("Reset to Flat") }
        }
    )
}

@Composable
fun BandSliderRow(
    band: EqualizerBandInfo,
    isEnabled: Boolean,
    onLevelChange: (Int) -> Unit
) {
    val rangeSpan = (band.maxLevelMilliBel - band.minLevelMilliBel).toFloat().coerceAtLeast(1f)
    val sliderValue = ((band.currentLevelMilliBel - band.minLevelMilliBel) / rangeSpan).coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = band.centerFreqFormatted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = band.levelDbFormatted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Slider(
            value = sliderValue,
            onValueChange = { frac ->
                val target = band.minLevelMilliBel + (frac * rangeSpan).toInt()
                onLevelChange(target)
            },
            enabled = isEnabled,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}