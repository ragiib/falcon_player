package com.example.falconplayer.ui.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.falconplayer.theme.FalconRed

@Composable
fun PlayerTopBar(
    title: String,
    hasSubtitlesOn: Boolean,
    onBackClick: () -> Unit,
    onAudioClick: () -> Unit = {},
    onSubtitlesClick: () -> Unit,
    onAspectRatioClick: () -> Unit,
    onPipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button & Clean Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Action Icons (Tracks/Subtitles, Aspect Ratio, PiP)
        // Redundant standalone music icon removed; Audio tracks are accessible via Tracks menu & Settings.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Subtitles & Audio Track Menu
            Box(contentAlignment = Alignment.TopEnd) {
                IconButton(onClick = onSubtitlesClick) {
                    Icon(
                        imageVector = Icons.Filled.Subtitles,
                        contentDescription = "Audio and Subtitles",
                        tint = if (hasSubtitlesOn) FalconRed else Color.White
                    )
                }
                if (hasSubtitlesOn) {
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp, end = 8.dp)
                            .size(7.dp)
                            .background(FalconRed, CircleShape)
                    )
                }
            }

            // Video Scaling / Aspect Ratio Toggle
            IconButton(onClick = onAspectRatioClick) {
                Icon(
                    imageVector = Icons.Filled.AspectRatio,
                    contentDescription = "Aspect Ratio",
                    tint = Color.White
                )
            }

            // Picture-in-Picture (PiP)
            IconButton(onClick = onPipClick) {
                Icon(
                    imageVector = Icons.Filled.PictureInPictureAlt,
                    contentDescription = "Picture-in-Picture",
                    tint = Color.White
                )
            }
        }
    }
}

