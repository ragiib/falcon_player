package com.example.falconplayer.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var hideControlsJob: Job? = null

    init {
        // Start playing state for dummy presentation
        _uiState.update { it.copy(playbackState = PlaybackState.Playing) }
        startHideControlsTimer()
    }

    fun onPlayPauseClick() {
        val currentState = _uiState.value.playbackState
        val newState = if (currentState == PlaybackState.Playing) {
            PlaybackState.Paused
        } else {
            PlaybackState.Playing
        }
        
        _uiState.update { it.copy(playbackState = newState) }
        
        if (newState == PlaybackState.Playing) {
            startHideControlsTimer()
        } else {
            cancelHideControlsTimer()
            showControls()
        }
    }

    fun onControlsScreenTap() {
        when (_uiState.value.controlsState) {
            ControlsState.Visible -> {
                hideControls()
            }
            ControlsState.Hidden -> {
                showControls()
                if (_uiState.value.playbackState == PlaybackState.Playing) {
                    startHideControlsTimer()
                }
            }
            ControlsState.Locked -> {
                // If locked, maybe show a brief "Unlock" hint, but don't show full controls
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
        if (_uiState.value.controlsState == ControlsState.Visible && 
            _uiState.value.playbackState == PlaybackState.Playing) {
            startHideControlsTimer()
        }
    }

    fun toggleSettingsSheet() {
        _uiState.update { it.copy(showSettingsSheet = !it.showSettingsSheet) }
        if (_uiState.value.showSettingsSheet) {
            cancelHideControlsTimer()
        } else {
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
}
