package com.example.falconplayer.ui.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.falconplayer.data.AlbumItem
import com.example.falconplayer.data.ArtistItem
import com.example.falconplayer.data.AudioItem
import com.example.falconplayer.data.AudioPlaybackManager
import com.example.falconplayer.data.AudioPlaybackState
import com.example.falconplayer.data.AudioRepository
import com.example.falconplayer.data.GenreItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AudioUiState(
    val isLoading: Boolean = false,
    val hasPermission: Boolean = true,
    val selectedTab: Int = 0, // 0: ARTISTS, 1: ALBUMS, 2: TRACKS, 3: GENRES, 4: PLAYLISTS
    val tracks: List<AudioItem> = emptyList(),
    val artists: List<ArtistItem> = emptyList(),
    val albums: List<AlbumItem> = emptyList(),
    val genres: List<GenreItem> = emptyList(),
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val showPlayerSheet: Boolean = false
) {
    val filteredArtists: List<ArtistItem>
        get() = if (searchQuery.isBlank()) artists else artists.filter { it.name.contains(searchQuery, ignoreCase = true) }

    val filteredAlbums: List<AlbumItem>
        get() = if (searchQuery.isBlank()) albums else albums.filter {
            it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true)
        }

    val filteredTracks: List<AudioItem>
        get() = if (searchQuery.isBlank()) tracks else tracks.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.artist.contains(searchQuery, ignoreCase = true) ||
            it.album.contains(searchQuery, ignoreCase = true)
        }

    val filteredGenres: List<GenreItem>
        get() = if (searchQuery.isBlank()) genres else genres.filter { it.name.contains(searchQuery, ignoreCase = true) }
}

@HiltViewModel
class AudioViewModel @Inject constructor(
    private val audioRepository: AudioRepository,
    val playbackManager: AudioPlaybackManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AudioUiState())
    val uiState: StateFlow<AudioUiState> = _uiState.asStateFlow()

    val playbackState: StateFlow<AudioPlaybackState> = playbackManager.playbackState

    init {
        loadAudio()
    }

    fun loadAudio() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val scannedTracks = audioRepository.getAudioTracks()
            val scannedArtists = audioRepository.getArtists(scannedTracks)
            val scannedAlbums = audioRepository.getAlbums(scannedTracks)
            val scannedGenres = audioRepository.getGenres(scannedTracks)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    tracks = scannedTracks,
                    artists = scannedArtists,
                    albums = scannedAlbums,
                    genres = scannedGenres
                )
            }
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        _uiState.update { it.copy(hasPermission = isGranted) }
        if (isGranted) {
            loadAudio()
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun toggleSearch() {
        _uiState.update { current ->
            if (current.isSearching) {
                current.copy(isSearching = false, searchQuery = "")
            } else {
                current.copy(isSearching = true)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun openPlayerSheet() {
        _uiState.update { it.copy(showPlayerSheet = true) }
    }

    fun closePlayerSheet() {
        _uiState.update { it.copy(showPlayerSheet = false) }
    }

    fun playTrack(track: AudioItem) {
        val currentTabTracks = _uiState.value.tracks
        playbackManager.playTrack(track, currentTabTracks)
        openPlayerSheet()
    }

    fun playTracks(tracks: List<AudioItem>) {
        playbackManager.playQueue(tracks, 0)
        openPlayerSheet()
    }
}
