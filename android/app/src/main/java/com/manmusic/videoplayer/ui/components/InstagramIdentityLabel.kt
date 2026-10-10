package com.manmusic.videoplayer.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manmusic.videoplayer.R

/**
 * Reusable Jetpack Compose component displaying the creator identity "@realmanjoor".
 * - Placed at the top of every main screen.
 * - Uses technical monospace typography with high contrast.
 * - Tappable: launches Instagram intent / web URL.
 * - Automatically derives styling and contrast from MaterialTheme colors.
 */
@Composable
fun InstagramIdentityLabel(
    modifier: Modifier = Modifier,
    customHandle: String = stringResource(R.string.instagram_creator),
    instagramUrl: String = stringResource(R.string.instagram_profile_url)
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    val isDark = colorScheme.background.red < 0.5f
    val badgeBg = if (isDark) colorScheme.surfaceVariant.copy(alpha = 0.85f) else colorScheme.surfaceVariant.copy(alpha = 0.95f)
    val badgeBorderColor = if (isDark) colorScheme.primary.copy(alpha = 0.45f) else colorScheme.primary.copy(alpha = 0.35f)
    val textColor = colorScheme.onSurface

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true, color = colorScheme.primary),
                role = Role.Button,
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(instagramUrl)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            ),
        shape = RoundedCornerShape(20.dp),
        color = badgeBg,
        border = BorderStroke(1.dp, badgeBorderColor),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCB045))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.9f)))
            }

            Text(
                text = customHandle,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.6.sp,
                    fontSize = 11.sp
                ),
                color = textColor
            )
        }
    }
}