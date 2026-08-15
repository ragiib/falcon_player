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

private const val TAG = "FavoritesRepository"
private const val FILE_NAME = "falcon_favorites.json"

@Singleton
class FavoritesRepository @Inject constructor(
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

    private val _favoriteUris = MutableStateFlow<List<String>>(emptyList())
    val favoriteUris: StateFlow<List<String>> = _favoriteUris.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        CoroutineScope(Dispatchers.IO).launch {
            mutex.withLock {
                try {
                    if (file.exists()) {
                        val content = file.readText()
                        if (content.isNotBlank()) {
                            val list = json.decodeFromString<List<String>>(content)
                            _favoriteUris.value = list
                            Log.d(TAG, "Loaded ${list.size} favorite items from storage")
                            return@withLock
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading favorites file", e)
                }
                _favoriteUris.value = emptyList()
            }
        }
    }

    private suspend fun saveFavoritesToFile(list: List<String>) = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                val content = json.encodeToString(list)
                file.writeText(content)
                _favoriteUris.value = list
                Log.d(TAG, "Saved ${list.size} favorites to storage")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving favorites file", e)
            }
        }
    }

    suspend fun toggleFavorite(uriStr: String) {
        if (uriStr.isBlank()) return
        val currentList = _favoriteUris.value
        val updated = if (uriStr in currentList) {
            currentList - uriStr
        } else {
            currentList + uriStr
        }
        _favoriteUris.value = updated
        saveFavoritesToFile(updated)
    }

    fun isFavorite(uriStr: String): Boolean {
        return uriStr in _favoriteUris.value
    }
}
