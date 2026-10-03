package com.example.falconplayer.ui.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.falconplayer.theme.FalconRed
import com.example.falconplayer.ui.player.ControlsState
import com.example.falconplayer.ui.player.MediaInfo

@Composable
fun PlayerBottomControls(
    mediaInfo: MediaInfo,
    controlsState: ControlsState,
    playbackSpeed: Float,
    onSeek: (Long) -> Unit,
    onLockToggle: () -> Unit,
    onSpeedClick: () -> Unit,
    onFullscreenToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLocked = controlsState == ControlsState.Locked

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        if (!isLocked) {
            // High-End Minimal Touch-Friendly Video Timeline
            VideoTimeline(
                currentPositionMs = mediaInfo.currentPositionMs,
                durationMs = mediaInfo.durationMs,
                bufferedPositionMs = mediaInfo.bufferedPositionMs,
                onSeek = onSeek
            )
        }

        // Secondary Actions Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lock Button
            IconButton(onClick = onLockToggle) {
                Icon(
                    imageVector = if (isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                    contentDescription = if (isLocked) "Unlock Controls" else "Lock Controls",
                    tint = if (isLocked) FalconRed else Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            if (!isLocked) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Unified Playback Speed Control
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (playbackSpeed != 1.0f) FalconRed else Color.White.copy(alpha = 0.15f))
                            .clickable(onClick = onSpeedClick)
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Speed,
                                contentDescription = "Playback Speed",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${playbackSpeed}x",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Fullscreen / Orientation Toggle
                    IconButton(onClick = onFullscreenToggle) {
                        Icon(
                            imageVector = Icons.Filled.Fullscreen,
                            contentDescription = "Toggle Orientation / Fullscreen",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}
