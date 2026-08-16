package com.example.falconplayer.ui.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.falconplayer.data.BrowseFolderItem
import com.example.falconplayer.data.BrowseNodeItem
import com.example.falconplayer.data.BrowseRepository
import com.example.falconplayer.data.StorageItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class BrowseUiState(
    val currentPath: String? = null,
    val currentFolderName: String = "",
    val favorites: List<BrowseFolderItem> = emptyList(),
    val storages: List<StorageItem> = emptyList(),
    val directoryContents: List<BrowseNodeItem> = emptyList(),
    val filteredContents: List<BrowseNodeItem> = emptyList(),
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val pathHistory: List<String> = emptyList()
)

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val browseRepository: BrowseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowseUiState())
    val uiState: StateFlow<BrowseUiState> = _uiState.asStateFlow()

    init {
        loadRootData()
    }

    fun loadRootData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val favorites = browseRepository.getFavorites()
            val storages = browseRepository.getStorages()
            _uiState.update {
                it.copy(
                    favorites = favorites,
                    storages = storages,
                    isLoading = false
                )
            }
        }
    }

    fun openDirectory(path: String, folderName: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                val newHistory = if (state.currentPath != null && state.currentPath != path) {
                    state.pathHistory + state.currentPath
                } else {
                    state.pathHistory
                }
                state.copy(
                    currentPath = path,
                    currentFolderName = folderName,
                    pathHistory = newHistory,
                    isSearching = false,
                    searchQuery = "",
                    isLoading = true
                )
            }

            val contents = browseRepository.getDirectoryContents(path)
            _uiState.update {
                it.copy(
                    directoryContents = contents,
                    filteredContents = contents,
                    isLoading = false
                )
            }
        }
    }

    fun navigateUp(): Boolean {
        val state = _uiState.value
        if (state.isSearching) {
            closeSearch()
            return true
        }

        if (state.pathHistory.isNotEmpty()) {
            val previousPath = state.pathHistory.last()
            val newHistory = state.pathHistory.dropLast(1)
            val parentName = File(previousPath).name.ifEmpty { "Folder" }
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        currentPath = previousPath,
                        currentFolderName = parentName,
                        pathHistory = newHistory,
                        isLoading = true
                    )
                }
                val contents = browseRepository.getDirectoryContents(previousPath)
                _uiState.update {
                    it.copy(
                        directoryContents = contents,
                        filteredContents = contents,
                        isLoading = false
                    )
                }
            }
            return true
        }

        if (state.currentPath != null) {
            val currentFile = File(state.currentPath)
            val parentFile = currentFile.parentFile
            if (parentFile != null && parentFile.exists() && parentFile.canRead() && parentFile.absolutePath.startsWith("/storage")) {
                openDirectory(parentFile.absolutePath, parentFile.name)
            } else {
                // Return to root Browse screen
                _uiState.update {
                    it.copy(
                        currentPath = null,
                        currentFolderName = "",
                        pathHistory = emptyList(),
                        directoryContents = emptyList(),
                        filteredContents = emptyList()
                    )
                }
                loadRootData()
            }
            return true
        }

        return false
    }

    fun openSearch() {
        _uiState.update { it.copy(isSearching = true) }
    }

    fun closeSearch() {
        _uiState.update {
            it.copy(
                isSearching = false,
                searchQuery = "",
                filteredContents = it.directoryContents
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            val filtered = if (query.isBlank()) {
                state.directoryContents
            } else {
                state.directoryContents.filter { node ->
                    when (node) {
                        is BrowseNodeItem.FolderNode -> node.folder.name.contains(query, ignoreCase = true)
                        is BrowseNodeItem.FileNode -> node.file.name.contains(query, ignoreCase = true)
                    }
                }
            }
            state.copy(
                searchQuery = query,
                filteredContents = filtered
            )
        }
    }

    fun refresh() {
        val currentPath = _uiState.value.currentPath
        if (currentPath != null) {
            openDirectory(currentPath, _uiState.value.currentFolderName)
        } else {
            loadRootData()
        }
    }
}
