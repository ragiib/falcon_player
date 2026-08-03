package com.example.falconplayer.ui.player

data class PlayerUiState(
    val mediaInfo: MediaInfo = MediaInfo(),
    val playbackState: PlaybackState = PlaybackState.Idle,
    val controlsState: ControlsState = ControlsState.Visible,
    val isFullscreen: Boolean = false,
    val showSettingsSheet: Boolean = false
)

data class MediaInfo(
    val title: String = "Big Buck Bunny - Sample Video",
    val currentPositionMs: Long = 120000L, // 2 minutes in
    val durationMs: Long = 596000L, // ~10 minutes
    val isLive: Boolean = false
)

sealed interface PlaybackState {
    object Idle : PlaybackState
    object Buffering : PlaybackState
    object Playing : PlaybackState
    object Paused : PlaybackState
    object Ended : PlaybackState
    data class Error(val message: String) : PlaybackState
}

sealed interface ControlsState {
    object Visible : ControlsState
    object Hidden : ControlsState
    object Locked : ControlsState
}
