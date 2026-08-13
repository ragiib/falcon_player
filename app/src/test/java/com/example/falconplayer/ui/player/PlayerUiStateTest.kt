package com.example.falconplayer.ui.player

import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerUiStateTest {

    @Test
    fun defaultMediaInfo_startsAtZeroPositionAndDuration() {
        val state = PlayerUiState()
        assertEquals(0L, state.mediaInfo.currentPositionMs)
        assertEquals(0L, state.mediaInfo.durationMs)
    }

    @Test
    fun defaultPlaybackState_isIdle() {
        val state = PlayerUiState()
        assertEquals(PlaybackState.Idle, state.playbackState)
    }

    @Test
    fun playbackState_statusText_returnsExpectedLabels() {
        assertEquals("Loading", PlaybackState.Buffering.statusText)
        assertEquals("Playing", PlaybackState.Playing.statusText)
        assertEquals("Paused", PlaybackState.Paused.statusText)
        assertEquals("Paused", PlaybackState.Idle.statusText)
        assertEquals("Paused", PlaybackState.Ended.statusText)
        assertEquals("Error", PlaybackState.Error("Sample error").statusText)
    }
}
