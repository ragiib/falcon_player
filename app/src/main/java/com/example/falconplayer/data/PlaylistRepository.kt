package com.example.falconplayer.data

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PlaylistRepository"
private const val FILE_NAME = "falcon_playlists.json"
private const val MAX_NAME_LENGTH = 50

sealed class PlaylistValidationResult {
    data object Success : PlaylistValidationResult()
    data class Error(val message: String) : PlaylistValidationResult()
}

@Singleton
class PlaylistRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val file: File
        get() = File(context.filesDir, FILE_NAME)

    private val mutex = Mutex()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        CoroutineScope(Dispatchers.IO).launch {
            mutex.withLock {
                try {
                    if (file.exists()) {
                        val content = file.readText()
                        if (content.isNotBlank()) {
                            val list = json.decodeFromString<List<Playlist>>(content)
                            _playlists.value = list
                            Log.d(TAG, "Loaded ${list.size} playlists from storage")
                            return@withLock
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading playlists file", e)
                }
                _playlists.value = emptyList()
            }
        }
    }

    private suspend fun savePlaylistsToFile(list: List<Playlist>) = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                val content = json.encodeToString(list)
                file.writeText(content)
                _playlists.value = list
                Log.d(TAG, "Saved ${list.size} playlists to storage")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving playlists file", e)
            }
        }
    }

    fun validatePlaylistName(name: String, excludeId: String? = null): PlaylistValidationResult {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            return PlaylistValidationResult.Error("Playlist name cannot be empty.")
        }
        if (trimmed.length > MAX_NAME_LENGTH) {
            return PlaylistValidationResult.Error("Playlist name cannot exceed $MAX_NAME_LENGTH characters.")
        }
        val exists = _playlists.value.any {
            it.id != excludeId && it.name.equals(trimmed, ignoreCase = true)
        }
        if (exists) {
            return PlaylistValidationResult.Error("A playlist with the name '$trimmed' already exists.")
        }
        return PlaylistValidationResult.Success
    }

    suspend fun createPlaylist(name: String): PlaylistValidationResult {
        val validation = validatePlaylistName(name)
        if (validation is PlaylistValidationResult.Error) {
            return validation
        }
        val trimmed = name.trim()
        val newPlaylist = Playlist(name = trimmed)
        val updated = _playlists.value + newPlaylist
        _playlists.value = updated
        savePlaylistsToFile(updated)
        return PlaylistValidationResult.Success
    }

    suspend fun renamePlaylist(playlistId: String, newName: String): PlaylistValidationResult {
        val validation = validatePlaylistName(newName, excludeId = playlistId)
        if (validation is PlaylistValidationResult.Error) {
            return validation
        }
        val trimmed = newName.trim()
        val updated = _playlists.value.map {
            if (it.id == playlistId) it.copy(name = trimmed) else it
        }
        _playlists.value = updated
        savePlaylistsToFile(updated)
        return PlaylistValidationResult.Success
    }

    suspend fun deletePlaylist(playlistId: String) {
        val updated = _playlists.value.filterNot { it.id == playlistId }
        _playlists.value = updated
        savePlaylistsToFile(updated)
    }

    suspend fun addVideosToPlaylist(playlistId: String, newUris: List<String>) {
        val updated = _playlists.value.map { playlist ->
            if (playlist.id == playlistId) {
                val existingSet = playlist.videoUris.toSet()
                val uniqueToAdd = newUris.filter { it !in existingSet }
                playlist.copy(videoUris = playlist.videoUris + uniqueToAdd)
            } else {
                playlist
            }
        }
        _playlists.value = updated
        savePlaylistsToFile(updated)
    }

    suspend fun removeVideoFromPlaylist(playlistId: String, videoUri: String) {
        val updated = _playlists.value.map { playlist ->
            if (playlist.id == playlistId) {
                playlist.copy(videoUris = playlist.videoUris.filterNot { it == videoUri })
            } else {
                playlist
            }
        }
        _playlists.value = updated
        savePlaylistsToFile(updated)
    }

    suspend fun reorderPlaylist(playlistId: String, newVideoUris: List<String>) {
        val updated = _playlists.value.map { playlist ->
            if (playlist.id == playlistId) {
                playlist.copy(videoUris = newVideoUris)
            } else {
                playlist
            }
        }
        _playlists.value = updated
        savePlaylistsToFile(updated)
    }

    suspend fun moveVideoInPlaylist(playlistId: String, fromIndex: Int, toIndex: Int) {
        val playlist = _playlists.value.find { it.id == playlistId } ?: return
        if (fromIndex < 0 || fromIndex >= playlist.videoUris.size || toIndex < 0 || toIndex >= playlist.videoUris.size) return

        val mutableList = playlist.videoUris.toMutableList()
        val item = mutableList.removeAt(fromIndex)
        mutableList.add(toIndex, item)

        reorderPlaylist(playlistId, mutableList)
    }
}
