package com.example.falconplayer.ui.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.falconplayer.data.DisplaySettingsData
import com.example.falconplayer.data.DisplaySettingsRepository
import com.example.falconplayer.data.FavoritesRepository
import com.example.falconplayer.data.FolderItem
import com.example.falconplayer.data.HistoryRepository
import com.example.falconplayer.data.SortType
import com.example.falconplayer.data.VideoItem
import com.example.falconplayer.data.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val hasPermission: Boolean = false,
    val isLoading: Boolean = false,
    val videos: List<VideoItem> = emptyList(),
    val folders: List<FolderItem> = emptyList(),
    val selectedFolderBucketId: String? = null,
    val selectedFolderName: String? = null,
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val isHistoryActive: Boolean = false,
    val historyVideos: List<VideoItem> = emptyList(),
    val isListView: Boolean = false,
    val showOnlyFavorites: Boolean = false,
    val incognitoMode: Boolean = false,
    val groupOption: String = "Group by name",
    val playbackAction: String = "Play",
    val sortType: SortType = SortType.NAME_ASC,
    val favoriteUris: List<String> = emptyList(),
    val showDisplaySettingsScreen: Boolean = false
) {
    val filteredVideos: List<VideoItem>
        get() {
            val baseList = when {
                isSearching -> {
                    if (searchQuery.isBlank()) {
                        videos
                    } else {
                        videos.filter { it.title.contains(searchQuery, ignoreCase = true) }
                    }
                }
                selectedFolderBucketId != null -> {
                    videos.filter { it.bucketId == selectedFolderBucketId }
                }
                else -> videos
            }

            val favFiltered = if (showOnlyFavorites) {
                baseList.filter { it.contentUri.toString() in favoriteUris }
            } else {
                baseList
            }

            val folderCounts = folders.associate { it.bucketId to it.videoCount }

            return when (sortType) {
                SortType.NAME_ASC -> favFiltered.sortedBy { it.title.lowercase() }
                SortType.NAME_DESC -> favFiltered.sortedByDescending { it.title.lowercase() }
                SortType.LENGTH_ASC -> favFiltered.sortedBy { it.durationMs }
                SortType.LENGTH_DESC -> favFiltered.sortedByDescending { it.durationMs }
                SortType.ADDED_ASC, SortType.INSERTION_ASC -> favFiltered.sortedBy { if (it.dateAddedSec > 0) it.dateAddedSec else it.id }
                SortType.ADDED_DESC, SortType.INSERTION_DESC -> favFiltered.sortedByDescending { if (it.dateAddedSec > 0) it.dateAddedSec else it.id }
                SortType.TRACKS_DESC -> favFiltered.sortedByDescending { folderCounts[it.bucketId] ?: 0 }
                SortType.TRACKS_ASC -> favFiltered.sortedBy { folderCounts[it.bucketId] ?: 0 }
            }
        }

    val sortedFolders: List<FolderItem>
        get() {
            return when (sortType) {
                SortType.NAME_ASC -> folders.sortedBy { it.bucketName.lowercase() }
                SortType.NAME_DESC -> folders.sortedByDescending { it.bucketName.lowercase() }
                SortType.TRACKS_DESC -> folders.sortedByDescending { it.videoCount }
                SortType.TRACKS_ASC -> folders.sortedBy { it.videoCount }
                SortType.ADDED_DESC, SortType.INSERTION_DESC -> folders.sortedByDescending { f -> f.previewVideos.maxOfOrNull { it.dateAddedSec } ?: 0L }
                SortType.ADDED_ASC, SortType.INSERTION_ASC -> folders.sortedBy { f -> f.previewVideos.minOfOrNull { it.dateAddedSec } ?: 0L }
                else -> folders
            }
        }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val historyRepository: HistoryRepository,
    private val displaySettingsRepository: DisplaySettingsRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeHistory()
        observeDisplaySettings()
        observeFavorites()
    }

    private fun observeHistory() {
        viewModelScope.launch {
            historyRepository.historyUris.collect { uris ->
                updateHistoryVideos(uris, _uiState.value.videos)
            }
        }
    }

    private fun observeDisplaySettings() {
        viewModelScope.launch {
            displaySettingsRepository.settings.collect { settings ->
                _uiState.update {
                    it.copy(
                        isListView = settings.isListView,
                        showOnlyFavorites = settings.showOnlyFavorites,
                        incognitoMode = settings.incognitoMode,
                        groupOption = settings.groupOption,
                        playbackAction = settings.playbackAction,
                        sortType = settings.sortType
                    )
                }
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            favoritesRepository.favoriteUris.collect { favs ->
                _uiState.update { it.copy(favoriteUris = favs) }
            }
        }
    }

    private fun updateHistoryVideos(uris: List<String>, allVideos: List<VideoItem>) {
        val videoMap = allVideos.associateBy { it.contentUri.toString() }
        val mapped = uris.mapNotNull { uriStr ->
            videoMap[uriStr] ?: try {
                val uri = Uri.parse(uriStr)
                VideoItem(
                    id = uriStr.hashCode().toLong(),
                    contentUri = uri,
                    title = uri.lastPathSegment ?: "Video",
                    durationMs = 0L,
                    width = 0,
                    height = 0,
                    sizeBytes = 0L,
                    bucketId = "history",
                    bucketName = "History"
                )
            } catch (e: Exception) {
                null
            }
        }
        _uiState.update { it.copy(historyVideos = mapped) }
    }

    fun onPermissionResult(isGranted: Boolean) {
        _uiState.update { it.copy(hasPermission = isGranted) }
        if (isGranted) {
            loadMedia()
        }
    }

    fun loadMedia() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val scannedVideos = videoRepository.getVideos()
                val scannedFolders = videoRepository.getFolders(scannedVideos)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        videos = scannedVideos,
                        folders = scannedFolders
                    )
                }
                updateHistoryVideos(historyRepository.historyUris.value, scannedVideos)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectFolder(bucketId: String, bucketName: String) {
        _uiState.update {
            it.copy(
                selectedFolderBucketId = bucketId,
                selectedFolderName = bucketName
            )
        }
    }

    fun clearFolderFilter() {
        _uiState.update {
            it.copy(
                selectedFolderBucketId = null,
                selectedFolderName = null
            )
        }
    }

    fun openSearch() {
        _uiState.update {
            it.copy(
                isSearching = true,
                isHistoryActive = false,
                showDisplaySettingsScreen = false,
                searchQuery = ""
            )
        }
    }

    fun closeSearch() {
        _uiState.update {
            it.copy(
                isSearching = false,
                searchQuery = ""
            )
        }
    }

    fun openHistory() {
        _uiState.update {
            it.copy(
                isHistoryActive = true,
                isSearching = false,
                showDisplaySettingsScreen = false,
                searchQuery = ""
            )
        }
    }

    fun closeHistory() {
        _uiState.update {
            it.copy(isHistoryActive = false)
        }
    }

    fun openDisplaySettings() {
        _uiState.update {
            it.copy(
                showDisplaySettingsScreen = true,
                isSearching = false,
                isHistoryActive = false
            )
        }
    }

    fun closeDisplaySettings() {
        _uiState.update {
            it.copy(showDisplaySettingsScreen = false)
        }
    }

    fun toggleIncognitoMode() {
        viewModelScope.launch {
            displaySettingsRepository.updateSettings {
                it.copy(incognitoMode = !it.incognitoMode)
            }
        }
    }

    fun toggleListView() {
        viewModelScope.launch {
            displaySettingsRepository.updateSettings {
                it.copy(isListView = !it.isListView)
            }
        }
    }

    fun toggleShowOnlyFavorites() {
        viewModelScope.launch {
            displaySettingsRepository.updateSettings {
                it.copy(showOnlyFavorites = !it.showOnlyFavorites)
            }
        }
    }

    fun setGroupOption(option: String) {
        viewModelScope.launch {
            displaySettingsRepository.updateSettings {
                it.copy(groupOption = option)
            }
        }
    }

    fun setPlaybackAction(action: String) {
        viewModelScope.launch {
            displaySettingsRepository.updateSettings {
                it.copy(playbackAction = action)
            }
        }
    }

    fun setSortType(sortType: SortType) {
        viewModelScope.launch {
            displaySettingsRepository.updateSettings {
                it.copy(sortType = sortType)
            }
        }
    }

    fun toggleFavorite(uriStr: String) {
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(uriStr)
        }
    }

    fun recordVideoPlayed(uriStr: String) {
        viewModelScope.launch {
            historyRepository.recordVideoPlayed(uriStr)
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update {
            it.copy(searchQuery = query)
        }
    }
}
