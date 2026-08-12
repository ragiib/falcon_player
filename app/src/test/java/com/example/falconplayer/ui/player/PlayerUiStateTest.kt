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
}
