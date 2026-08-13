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
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
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

val PlaybackState.statusText: String
    get() = when (this) {
        PlaybackState.Buffering -> "Loading"
        PlaybackState.Playing -> "Playing"
        PlaybackState.Paused, PlaybackState.Idle, PlaybackState.Ended -> "Paused"
        is PlaybackState.Error -> "Error"
    }

sealed interface ControlsState {
    object Visible : ControlsState
    object Hidden : ControlsState
    object Locked : ControlsState
}
