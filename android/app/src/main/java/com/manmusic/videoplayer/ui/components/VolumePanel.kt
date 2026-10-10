package com.manmusic.videoplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manmusic.videoplayer.data.media.VolumeState

/**
 * Premium Volume Panel featuring gradient slider, animated thumb,
 * dynamic speaker icon, mute/unmute control, and compact/expanded modes.
 * Controls strictly AudioManager.STREAM_MUSIC.
 */
@Composable
fun VolumePanel(
    volumeState: VolumeState,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onToggleExpanded: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedRatio by animateFloatAsState(
        targetValue = volumeState.volumeRatio,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "volume_ratio"
    )

    val speakerIcon = when {
        volumeState.isMuted || volumeState.volumeRatio <= 0.01f -> Icons.Default.VolumeMute
        volumeState.volumeRatio < 0.4f -> Icons.Default.VolumeDown
        else -> Icons.Default.VolumeUp
    }

    val isHighVolume = volumeState.percentage > 85

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Main Compact Volume Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (volumeState.isMuted) Color(0xFFF43F5E).copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = speakerIcon,
                        contentDescription = "Mute Toggle",
                        tint = if (volumeState.isMuted) Color(0xFFF43F5E) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Slider(
                        value = volumeState.volumeRatio,
                        onValueChange = onVolumeChange,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = if (isHighVolume) Color(0xFFF43F5E) else MaterialTheme.colorScheme.primary,
                            activeTrackColor = Color.Transparent,
                            inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                        )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = animatedRatio)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            if (isHighVolume) Color(0xFFF43F5E) else MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.defaultMinSize(minWidth = 42.dp)
                ) {
                    Text(
                        text = "${volumeState.percentage}%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isHighVolume) Color(0xFFF43F5E) else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                IconButton(
                    onClick = { onToggleExpanded(!volumeState.isExpanded) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (volumeState.isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand Volume Details",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(visible = volumeState.isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 4.dp, end = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Text(
                                text = "STREAM_MUSIC (Media Playback Only)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "Level: ${volumeState.currentVolume} / ${volumeState.maxVolume}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (isHighVolume) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF43F5E).copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF43F5E), modifier = Modifier.size(16.dp))
                                Text(
                                    text = "High volume level. Hearing protection recommended for headphones.",
                                    fontSize = 10.sp,
                                    color = Color(0xFFF43F5E),
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }

                    Text(
                        text = "Ringtone & alarm sound streams remain unaffected and preserved.",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}