package com.example.falconplayer.ui.player.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.falconplayer.theme.FalconRed
import com.example.falconplayer.ui.player.PlaybackState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerCenterControls(
    playbackState: PlaybackState,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onRewindClick: () -> Unit = {},
    onForwardClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isPlaying = playbackState == PlaybackState.Playing

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous (Minimal circular disc: 40dp visual, 52dp comfortable touch target)
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .combinedClickable(
                    onClick = onPreviousClick,
                    onLongClick = onRewindClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .border(
                        BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipPrevious,
                    contentDescription = "Previous",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(18.dp))

        // Play/Pause (Central restrained button: 48dp visual, 56dp touch target)
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .combinedClickable(
                    onClick = onPlayPauseClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(FalconRed)
                    .border(
                        BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier
                        .size(26.dp)
                        .then(
                            // Optical alignment: slightly offset arrow to center the visual mass of the triangle
                            if (!isPlaying) Modifier.padding(start = 2.dp) else Modifier
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(18.dp))

        // Next (Minimal circular disc: 40dp visual, 52dp comfortable touch target)
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .combinedClickable(
                    onClick = onNextClick,
                    onLongClick = onForwardClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .border(
                        BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

