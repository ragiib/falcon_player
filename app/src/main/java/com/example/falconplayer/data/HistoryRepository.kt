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

private const val TAG = "HistoryRepository"
private const val FILE_NAME = "falcon_history.json"

@Singleton
class HistoryRepository @Inject constructor(
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

    private val _historyUris = MutableStateFlow<List<String>>(emptyList())
    val historyUris: StateFlow<List<String>> = _historyUris.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        CoroutineScope(Dispatchers.IO).launch {
            mutex.withLock {
                try {
                    if (file.exists()) {
                        val content = file.readText()
                        if (content.isNotBlank()) {
                            val list = json.decodeFromString<List<String>>(content)
                            _historyUris.value = list
                            Log.d(TAG, "Loaded ${list.size} history items from storage")
                            return@withLock
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading history file", e)
                }
                _historyUris.value = emptyList()
            }
        }
    }

    private suspend fun saveHistoryToFile(list: List<String>) = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                val content = json.encodeToString(list)
                file.writeText(content)
                _historyUris.value = list
                Log.d(TAG, "Saved ${list.size} history items to storage")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving history file", e)
            }
        }
    }

    suspend fun recordVideoPlayed(uriStr: String) {
        if (uriStr.isBlank()) return
        val currentList = _historyUris.value.filterNot { it == uriStr }
        val updated = listOf(uriStr) + currentList
        _historyUris.value = updated
        saveHistoryToFile(updated)
    }

    suspend fun clearHistory() {
        _historyUris.value = emptyList()
        saveHistoryToFile(emptyList())
    }
}
