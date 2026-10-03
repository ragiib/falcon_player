package com.example.falconplayer.ui.player

import androidx.media3.common.TrackGroup
import androidx.media3.ui.AspectRatioFrameLayout

data class TrackItem(
    val id: String,
    val group: TrackGroup,
    val groupIndex: Int,
    val trackIndex: Int,
    val name: String,
    val language: String?,
    val label: String?,
    val technicalDetails: String?,
    val isSelected: Boolean,
    val isSupported: Boolean
)

enum class VideoResizeMode(val displayName: String, val mode: Int) {
    FIT("Fit (Original)", AspectRatioFrameLayout.RESIZE_MODE_FIT),
    ZOOM("Crop (Fill Screen)", AspectRatioFrameLayout.RESIZE_MODE_ZOOM),
    STRETCH("Stretch to Fill", AspectRatioFrameLayout.RESIZE_MODE_FILL)
}

enum class TrackTab {
    AUDIO,
    SUBTITLE
}

data class SeekFeedback(
    val isForward: Boolean,
    val amountSeconds: Int = 10,
    val timestamp: Long = System.currentTimeMillis()
)

data class PlayerUiState(
    val mediaInfo: MediaInfo = MediaInfo(),
    val playbackState: PlaybackState = PlaybackState.Idle,
    val controlsState: ControlsState = ControlsState.Visible,
    val isFullscreen: Boolean = false,
    val showSettingsSheet: Boolean = false,
    val showTrackSheet: Boolean = false,
    val trackSheetTab: TrackTab = TrackTab.AUDIO,
    val audioTracks: List<TrackItem> = emptyList(),
    val selectedAudioTrackId: String? = null,
    val subtitleTracks: List<TrackItem> = emptyList(),
    val selectedSubtitleTrackId: String? = null,
    val resizeMode: VideoResizeMode = VideoResizeMode.FIT,
    val resizeModeMessage: String? = null,
    val playbackSpeed: Float = 1.0f,
    val seekFeedback: SeekFeedback? = null,
    val showInfoDialog: Boolean = false,
    val videoWidth: Int = 0,
    val videoHeight: Int = 0
)

data class MediaInfo(
    val title: String = "Video",
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
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
