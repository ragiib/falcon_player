package com.example.falconplayer.ui.player

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.falconplayer.data.PlaybackPositionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val positionRepository: PlaybackPositionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    val player: ExoPlayer = ExoPlayer.Builder(context).build()

    private var hideControlsJob: Job? = null
    private var progressJob: Job? = null
    private var currentUri: String? = null

    init {
        setupPlayerListeners()
    }

    private fun setupPlayerListeners() {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlaybackState()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlaybackState()
                if (isPlaying) {
                    startProgressTicker()
                    startHideControlsTimer()
                } else {
                    stopProgressTicker()
                    cancelHideControlsTimer()
                    showControls()
                    saveCurrentPosition()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _uiState.update { it.copy(playbackState = PlaybackState.Error(error.message ?: "Unknown error")) }
            }
        })
    }

    private fun updatePlaybackState() {
        val newState = when (player.playbackState) {
            Player.STATE_IDLE -> PlaybackState.Idle
            Player.STATE_BUFFERING -> PlaybackState.Buffering
            Player.STATE_READY -> if (player.isPlaying) PlaybackState.Playing else PlaybackState.Paused
            Player.STATE_ENDED -> PlaybackState.Ended
            else -> PlaybackState.Idle
        }
        _uiState.update { it.copy(playbackState = newState) }
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                updateMediaInfo()
                delay(200L) // Update every 200ms
            }
        }
    }

    private fun stopProgressTicker() {
        progressJob?.cancel()
        progressJob = null
        updateMediaInfo() // Final update
    }

    @OptIn(UnstableApi::class)
    private fun updateMediaInfo() {
        _uiState.update { state ->
            state.copy(
                mediaInfo = state.mediaInfo.copy(
                    currentPositionMs = player.currentPosition.coerceAtLeast(0L),
                    durationMs = player.duration.coerceAtLeast(0L),
                    isLive = player.isCurrentMediaItemLive
                )
            )
        }
    }

    fun loadVideo(uri: Uri, title: String = uri.lastPathSegment ?: "Unknown Video") {
        val uriString = uri.toString()
        currentUri = uriString
        
        _uiState.update { 
            it.copy(
                mediaInfo = it.mediaInfo.copy(title = title, currentPositionMs = 0L, durationMs = 0L),
                playbackState = PlaybackState.Buffering
            ) 
        }

        viewModelScope.launch {
            val mediaItem = MediaItem.fromUri(uri)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.seekTo(0L)
            player.play()
        }
    }

    private fun saveCurrentPosition() {
        currentUri?.let { uri ->
            viewModelScope.launch {
                positionRepository.savePosition(uri, player.currentPosition)
            }
        }
    }

    fun onPlayPauseClick() {
        if (player.playbackState == Player.STATE_ENDED) {
            player.seekTo(0)
            player.play()
        } else if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
        onUserActivity()
    }

    fun onSeek(positionMs: Long) {
        player.seekTo(positionMs)
        updateMediaInfo()
        onUserActivity()
    }

    fun onRewindClick() {
        player.seekTo((player.currentPosition - 10000).coerceAtLeast(0))
        onUserActivity()
    }

    fun onForwardClick() {
        player.seekTo((player.currentPosition + 10000).coerceAtMost(player.duration))
        onUserActivity()
    }

    fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
        onUserActivity()
    }

    // UI Control Logic (Unchanged)
    fun onControlsScreenTap() {
        when (_uiState.value.controlsState) {
            ControlsState.Visible -> {
                hideControls()
            }
            ControlsState.Hidden -> {
                showControls()
                if (player.isPlaying) {
                    startHideControlsTimer()
                }
            }
            ControlsState.Locked -> {
                showControls()
                startHideControlsTimer()
            }
        }
    }

    fun onLockToggle() {
        _uiState.update { 
            val newControlsState = if (it.controlsState == ControlsState.Locked) {
                ControlsState.Visible
            } else {
                ControlsState.Locked
            }
            it.copy(controlsState = newControlsState)
        }
        startHideControlsTimer()
    }

    fun onUserActivity() {
        if (_uiState.value.controlsState == ControlsState.Visible && player.isPlaying) {
            startHideControlsTimer()
        }
    }

    fun toggleSettingsSheet() {
        _uiState.update { it.copy(showSettingsSheet = !it.showSettingsSheet) }
        if (_uiState.value.showSettingsSheet) {
            cancelHideControlsTimer()
        } else if (player.isPlaying) {
            startHideControlsTimer()
        }
    }

    fun toggleFullscreen() {
        _uiState.update { it.copy(isFullscreen = !it.isFullscreen) }
    }

    private fun showControls() {
        if (_uiState.value.controlsState != ControlsState.Locked) {
            _uiState.update { it.copy(controlsState = ControlsState.Visible) }
        }
    }

    private fun hideControls() {
        if (!_uiState.value.showSettingsSheet) {
            _uiState.update { 
                it.copy(
                    controlsState = if (it.controlsState == ControlsState.Locked) ControlsState.Locked else ControlsState.Hidden 
                ) 
            }
        }
    }

    private fun startHideControlsTimer() {
        cancelHideControlsTimer()
        hideControlsJob = viewModelScope.launch {
            delay(3000L) // Hide after 3 seconds of inactivity
            hideControls()
        }
    }

    private fun cancelHideControlsTimer() {
        hideControlsJob?.cancel()
        hideControlsJob = null
    }

    override fun onCleared() {
        super.onCleared()
        saveCurrentPosition()
        player.release()
    }
}
