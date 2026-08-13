package com.example.falconplayer.ui.player.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.falconplayer.ui.player.PlaybackState
import com.example.falconplayer.ui.player.statusText

@Composable
fun PlaybackStatusIndicator(
    playbackState: PlaybackState,
    modifier: Modifier = Modifier
) {
    val statusText = playbackState.statusText

    val targetDotColor = when (playbackState) {
        PlaybackState.Buffering -> Color(0xFFFFC107) // Amber/Yellow for Loading
        PlaybackState.Playing -> Color(0xFF4CAF50)   // Green for Playing
        PlaybackState.Paused, PlaybackState.Idle, PlaybackState.Ended -> Color(0xFFB0BEC5) // Neutral Gray for Paused
        is PlaybackState.Error -> Color(0xFFE53935)  // Red for Error
    }

    val dotColor by animateColorAsState(
        targetValue = targetDotColor,
        label = "statusDotColor"
    )

    // Pulse animation for Loading state
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val dotAlpha by if (playbackState == PlaybackState.Buffering) {
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "loadingDotAlpha"
        )
    } else {
        rememberUpdatedState(1.0f)
    }

    Box(
        modifier = modifier
            .background(
                color = Color.Black.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = CircleShape
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(dotAlpha)
                    .background(color = dotColor, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = statusText,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
