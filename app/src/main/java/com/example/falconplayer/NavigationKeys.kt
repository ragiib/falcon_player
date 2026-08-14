package com.example.falconplayer

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Home : NavKey
@Serializable data class PlaylistDetail(val playlistId: String) : NavKey
@Serializable data class Player(
    val videoUri: String? = null,
    val title: String? = null,
    val playlistId: String? = null,
    val playlistIndex: Int = 0
) : NavKey

