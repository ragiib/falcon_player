package com.example.falconplayer.ui.player.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.falconplayer.theme.FalconRed

@Composable
fun VideoTimeline(
    currentPositionMs: Long,
    durationMs: Long,
    bufferedPositionMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalDuration = durationMs.coerceAtLeast(1L)
    var isDragging by remember { mutableStateOf(false) }
    var dragPositionMs by remember { mutableLongStateOf(0L) }

    val displayPositionMs = if (isDragging) dragPositionMs else currentPositionMs.coerceAtLeast(0L)
    val playedFraction = (displayPositionMs.toFloat() / totalDuration).coerceIn(0f, 1f)
    val bufferedFraction = (bufferedPositionMs.coerceAtLeast(0L).toFloat() / totalDuration).coerceIn(0f, 1f)

    val trackHeight by animateDpAsState(
        targetValue = if (isDragging) 5.5.dp else 3.5.dp,
        label = "timelineTrackHeight"
    )
    val thumbRadius by animateDpAsState(
        targetValue = if (isDragging) 8.dp else 5.dp,
        label = "timelineThumbRadius"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        // Interactive Scrubbing Bar (36dp touch target)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .pointerInput(totalDuration) {
                    detectTapGestures { offset ->
                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                        val targetMs = (fraction * totalDuration).toLong()
                        onSeek(targetMs)
                    }
                }
                .pointerInput(totalDuration) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                            dragPositionMs = (fraction * totalDuration).toLong()
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                            dragPositionMs = (fraction * totalDuration).toLong()
                        },
                        onDragEnd = {
                            onSeek(dragPositionMs)
                            isDragging = false
                        },
                        onDragCancel = {
                            isDragging = false
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val cornerRadius = CornerRadius(canvasHeight / 2, canvasHeight / 2)

                // 1. Inactive/Total duration track
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.22f),
                    size = Size(canvasWidth, canvasHeight),
                    cornerRadius = cornerRadius
                )

                // 2. Buffered progress track
                if (bufferedFraction > 0f) {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.45f),
                        size = Size(canvasWidth * bufferedFraction, canvasHeight),
                        cornerRadius = cornerRadius
                    )
                }

                // 3. Played progress track
                if (playedFraction > 0f) {
                    drawRoundRect(
                        color = FalconRed,
                        size = Size(canvasWidth * playedFraction, canvasHeight),
                        cornerRadius = cornerRadius
                    )
                }

                // 4. Scrubber Thumb
                val thumbCenterX = (canvasWidth * playedFraction).coerceIn(
                    thumbRadius.toPx(),
                    canvasWidth - thumbRadius.toPx()
                )
                val thumbCenterY = canvasHeight / 2

                drawCircle(
                    color = FalconRed,
                    radius = thumbRadius.toPx(),
                    center = Offset(thumbCenterX, thumbCenterY)
                )
                // Crisp white inner center for precision
                drawCircle(
                    color = Color.White,
                    radius = (thumbRadius.toPx() * 0.4f),
                    center = Offset(thumbCenterX, thumbCenterY)
                )
            }
        }

        // Time Indicators Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTime(displayPositionMs),
                color = if (isDragging) FalconRed else Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp,
                fontWeight = if (isDragging) FontWeight.Bold else FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = formatTime(totalDuration),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = (timeMs / 1000).coerceAtLeast(0L)
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
