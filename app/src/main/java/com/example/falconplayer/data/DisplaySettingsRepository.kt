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
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "DisplaySettingsRepo"
private const val FILE_NAME = "falcon_display_settings.json"

enum class SortType {
    NAME_ASC,
    NAME_DESC,
    LENGTH_ASC,
    LENGTH_DESC,
    ADDED_ASC,
    ADDED_DESC,
    TRACKS_DESC,
    TRACKS_ASC,
    INSERTION_ASC,
    INSERTION_DESC
}

@Serializable
data class DisplaySettingsData(
    val isListView: Boolean = false,
    val showOnlyFavorites: Boolean = false,
    val incognitoMode: Boolean = false,
    val groupOption: String = "Group by name",
    val playbackAction: String = "Play",
    val sortType: SortType = SortType.NAME_ASC
)

@Singleton
class DisplaySettingsRepository @Inject constructor(
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

    private val _settings = MutableStateFlow(DisplaySettingsData())
    val settings: StateFlow<DisplaySettingsData> = _settings.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        CoroutineScope(Dispatchers.IO).launch {
            mutex.withLock {
                try {
                    if (file.exists()) {
                        val content = file.readText()
                        if (content.isNotBlank()) {
                            val data = json.decodeFromString<DisplaySettingsData>(content)
                            _settings.value = data
                            Log.d(TAG, "Loaded display settings from storage")
                            return@withLock
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading settings file", e)
                }
                _settings.value = DisplaySettingsData()
            }
        }
    }

    private suspend fun saveSettingsToFile(data: DisplaySettingsData) = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                val content = json.encodeToString(data)
                file.writeText(content)
                _settings.value = data
                Log.d(TAG, "Saved display settings to storage")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving settings file", e)
            }
        }
    }

    suspend fun updateSettings(update: (DisplaySettingsData) -> DisplaySettingsData) {
        val updated = update(_settings.value)
        _settings.value = updated
        saveSettingsToFile(updated)
    }
}
