package com.example.falconplayer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "playback_positions")

@Singleton
class PlaybackPositionRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getSavedPosition(videoUri: String): Flow<Long> {
        val key = longPreferencesKey(videoUri)
        return context.dataStore.data.map { preferences ->
            preferences[key] ?: 0L
        }
    }

    suspend fun savePosition(videoUri: String, positionMs: Long) {
        val key = longPreferencesKey(videoUri)
        context.dataStore.edit { preferences ->
            preferences[key] = positionMs
        }
    }
}
