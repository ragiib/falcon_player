package com.example.falconplayer.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.falconplayer.data.Playlist
import com.example.falconplayer.data.PlaylistRepository
import com.example.falconplayer.data.PlaylistValidationResult
import com.example.falconplayer.data.VideoItem
import com.example.falconplayer.data.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistDetailUiState(
    val playlist: Playlist? = null,
    val videos: List<VideoItem> = emptyList(),
    val totalDurationMs: Long = 0L,
    val isLoading: Boolean = false
)

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val videoRepository: VideoRepository
) : ViewModel() {

    val playlists: StateFlow<List<Playlist>> = playlistRepository.playlists

    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog: StateFlow<Boolean> = _showCreateDialog.asStateFlow()

    private val _renameTarget = MutableStateFlow<Playlist?>(null)
    val renameTarget: StateFlow<Playlist?> = _renameTarget.asStateFlow()

    private val _deleteTarget = MutableStateFlow<Playlist?>(null)
    val deleteTarget: StateFlow<Playlist?> = _deleteTarget.asStateFlow()

    private val _addToPlaylistVideos = MutableStateFlow<List<VideoItem>>(emptyList())
    val addToPlaylistVideos: StateFlow<List<VideoItem>> = _addToPlaylistVideos.asStateFlow()

    private val _availableVideos = MutableStateFlow<List<VideoItem>>(emptyList())
    val availableVideos: StateFlow<List<VideoItem>> = _availableVideos.asStateFlow()

    fun loadAvailableVideos() {
        viewModelScope.launch {
            _availableVideos.value = videoRepository.getVideos()
        }
    }

    fun openCreateDialog() {
        android.util.Log.d("PlaylistViewModel", "openCreateDialog() called")
        _showCreateDialog.value = true
    }

    fun closeCreateDialog() {
        _showCreateDialog.value = false
    }

    fun openRenameDialog(playlist: Playlist) {
        _renameTarget.value = playlist
    }

    fun closeRenameDialog() {
        _renameTarget.value = null
    }

    fun openDeleteDialog(playlist: Playlist) {
        _deleteTarget.value = playlist
    }

    fun closeDeleteDialog() {
        _deleteTarget.value = null
    }

    fun openAddToPlaylistDialog(videos: List<VideoItem>) {
        _addToPlaylistVideos.value = videos
    }

    fun closeAddToPlaylistDialog() {
        _addToPlaylistVideos.value = emptyList()
    }

    fun createPlaylist(name: String): String? {
        android.util.Log.d("PlaylistViewModel", "createPlaylist() called with name='$name'")
        val validation = playlistRepository.validatePlaylistName(name)
        if (validation is PlaylistValidationResult.Error) {
            android.util.Log.d("PlaylistViewModel", "createPlaylist() validation error: ${validation.message}")
            return validation.message
        }
        viewModelScope.launch {
            android.util.Log.d("PlaylistViewModel", "createPlaylist() launching coroutine...")
            playlistRepository.createPlaylist(name)
            closeCreateDialog()
        }
        return null
    }

    fun renamePlaylist(playlistId: String, newName: String): String? {
        val validation = playlistRepository.validatePlaylistName(newName, excludeId = playlistId)
        if (validation is PlaylistValidationResult.Error) {
            return validation.message
        }
        viewModelScope.launch {
            playlistRepository.renamePlaylist(playlistId, newName)
            closeRenameDialog()
        }
        return null
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlistId)
            closeDeleteDialog()
        }
    }

    fun addVideosToPlaylist(playlistId: String, videos: List<VideoItem>) {
        val uris = videos.map { it.contentUri.toString() }
        viewModelScope.launch {
            playlistRepository.addVideosToPlaylist(playlistId, uris)
            closeAddToPlaylistDialog()
        }
    }

    fun removeVideoFromPlaylist(playlistId: String, videoUri: String) {
        viewModelScope.launch {
            playlistRepository.removeVideoFromPlaylist(playlistId, videoUri)
        }
    }

    fun moveVideoInPlaylist(playlistId: String, fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            playlistRepository.moveVideoInPlaylist(playlistId, fromIndex, toIndex)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getPlaylistDetailState(playlistId: String): StateFlow<PlaylistDetailUiState> {
        return playlists.flatMapLatest { playlistList ->
            flow {
                val targetPlaylist = playlistList.find { it.id == playlistId }
                if (targetPlaylist == null) {
                    emit(PlaylistDetailUiState(playlist = null, isLoading = false))
                } else {
                    val allScannedVideos = videoRepository.getVideos()
                    val videoMap = allScannedVideos.associateBy { it.contentUri.toString() }
                    
                    val orderedVideos = targetPlaylist.videoUris.mapNotNull { uriStr ->
                        videoMap[uriStr]
                    }

                    val totalDuration = orderedVideos.sumOf { it.durationMs }
                    emit(
                        PlaylistDetailUiState(
                            playlist = targetPlaylist,
                            videos = orderedVideos,
                            totalDurationMs = totalDuration,
                            isLoading = false
                        )
                    )
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = PlaylistDetailUiState(isLoading = true)
        )
    }
}
