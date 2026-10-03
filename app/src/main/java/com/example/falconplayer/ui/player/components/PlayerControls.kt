package com.example.falconplayer.ui.player.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.falconplayer.ui.player.ControlsState
import com.example.falconplayer.ui.player.PlayerUiState

@Composable
fun PlayerControls(
    uiState: PlayerUiState,
    onBackClick: () -> Unit,
    onAudioClick: () -> Unit,
    onSubtitlesClick: () -> Unit,
    onAspectRatioClick: () -> Unit,
    onPipClick: () -> Unit,
    onSpeedClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onRewindClick: () -> Unit,
    onForwardClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onLockToggle: () -> Unit,
    onFullscreenToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVisible = uiState.controlsState == ControlsState.Visible
    val isLocked = uiState.controlsState == ControlsState.Locked
    val showAnyControls = isVisible || isLocked

    AnimatedVisibility(
        visible = showAnyControls,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isVisible) Color.Black.copy(alpha = 0.35f) else Color.Transparent)
        ) {
            // Top Bar
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it },
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent)
                            )
                        )
                ) {
                    PlayerTopBar(
                        title = uiState.mediaInfo.title,
                        hasSubtitlesOn = uiState.selectedSubtitleTrackId != null,
                        onBackClick = onBackClick,
                        onAudioClick = onAudioClick,
                        onSubtitlesClick = onSubtitlesClick,
                        onAspectRatioClick = onAspectRatioClick,
                        onPipClick = onPipClick,
                        modifier = Modifier.padding(top = 20.dp)
                    )
                }
            }

            // Center Playback Controls
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                PlayerCenterControls(
                    playbackState = uiState.playbackState,
                    onPlayPauseClick = onPlayPauseClick,
                    onPreviousClick = onPreviousClick,
                    onNextClick = onNextClick,
                    onRewindClick = onRewindClick,
                    onForwardClick = onForwardClick
                )
            }

            // Bottom Controls
            AnimatedVisibility(
                visible = showAnyControls,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            if (isVisible) Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            ) else Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Transparent)
                            )
                        )
                ) {
                    PlayerBottomControls(
                        mediaInfo = uiState.mediaInfo,
                        controlsState = uiState.controlsState,
                        playbackSpeed = uiState.playbackSpeed,
                        onSeek = onSeek,
                        onLockToggle = onLockToggle,
                        onSpeedClick = onSpeedClick,
                        onFullscreenToggle = onFullscreenToggle,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                }
            }
        }
    }
}
