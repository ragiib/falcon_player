package com.example.falconplayer.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.falconplayer.data.FolderItem
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
    val selectedFolderName: String? = null
) {
    val filteredVideos: List<VideoItem>
        get() = if (selectedFolderBucketId == null) {
            videos
        } else {
            videos.filter { it.bucketId == selectedFolderBucketId }
        }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

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
}
