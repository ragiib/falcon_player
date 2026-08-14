package com.example.falconplayer

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.falconplayer.ui.home.HomeScreen
import com.example.falconplayer.ui.player.PlayerScreen
import com.example.falconplayer.ui.playlist.PlaylistDetailScreen

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Home)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Home> {
          HomeScreen(
            onPlayMedia = { uri, title ->
              backStack.add(Player(videoUri = uri?.toString(), title = title))
            },
            onOpenPlaylist = { playlistId ->
              backStack.add(PlaylistDetail(playlistId = playlistId))
            }
          )
        }
        entry<PlaylistDetail> { key ->
          PlaylistDetailScreen(
            playlistId = key.playlistId,
            onBackClick = { backStack.removeLastOrNull() },
            onPlayVideoInPlaylist = { uri, title, playlistId, index ->
              backStack.add(
                Player(
                  videoUri = uri?.toString(),
                  title = title,
                  playlistId = playlistId,
                  playlistIndex = index
                )
              )
            }
          )
        }
        entry<Player> { key ->
          PlayerScreen(
            onBackClick = { backStack.removeLastOrNull() },
            initialVideoUri = key.videoUri?.let { Uri.parse(it) },
            initialTitle = key.title,
            initialPlaylistId = key.playlistId,
            initialPlaylistIndex = key.playlistIndex
          )
        }
      },
  )
}


