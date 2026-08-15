package com.example.falconplayer.ui.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.falconplayer.data.FolderItem
import com.example.falconplayer.data.HistoryRepository
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
    val historyVideos: List<VideoItem> = emptyList()
) {
    val filteredVideos: List<VideoItem>
        get() = when {
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
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeHistory()
    }

    private fun observeHistory() {
        viewModelScope.launch {
            historyRepository.historyUris.collect { uris ->
                updateHistoryVideos(uris, _uiState.value.videos)
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
                searchQuery = ""
            )
        }
    }

    fun closeHistory() {
        _uiState.update {
            it.copy(isHistoryActive = false)
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
