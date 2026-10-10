package com.manmusic.videoplayer.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.recentSearchDataStore: DataStore<Preferences> by preferencesDataStore(name = "recent_searches")

/**
 * Repository for storing and retrieving the last 5 media search queries locally using Jetpack DataStore.
 * Allows users to quickly re-run past searches across Music and Video libraries.
 */
class RecentSearchRepository(private val context: Context) {

    companion object {
        const val MAX_RECENT_SEARCHES = 5
        private const val DELIMITER = ""
    }

    private object PreferencesKeys {
        val MUSIC_RECENT_SEARCHES = stringPreferencesKey("music_recent_searches")
        val VIDEO_RECENT_SEARCHES = stringPreferencesKey("video_recent_searches")
    }

    val musicRecentSearches: Flow<List<String>> = getRecentSearchesFlow(PreferencesKeys.MUSIC_RECENT_SEARCHES)
    val videoRecentSearches: Flow<List<String>> = getRecentSearchesFlow(PreferencesKeys.VIDEO_RECENT_SEARCHES)

    private fun getRecentSearchesFlow(key: Preferences.Key<String>): Flow<List<String>> {
        return context.recentSearchDataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { preferences ->
                val serialized = preferences[key] ?: ""
                parseSearches(serialized)
            }
    }

    suspend fun addMusicSearch(query: String) {
        addSearch(PreferencesKeys.MUSIC_RECENT_SEARCHES, query)
    }

    suspend fun addVideoSearch(query: String) {
        addSearch(PreferencesKeys.VIDEO_RECENT_SEARCHES, query)
    }

    suspend fun removeMusicSearch(query: String) {
        removeSearch(PreferencesKeys.MUSIC_RECENT_SEARCHES, query)
    }

    suspend fun removeVideoSearch(query: String) {
        removeSearch(PreferencesKeys.VIDEO_RECENT_SEARCHES, query)
    }

    suspend fun clearMusicSearches() {
        context.recentSearchDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.MUSIC_RECENT_SEARCHES)
        }
    }

    suspend fun clearVideoSearches() {
        context.recentSearchDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.VIDEO_RECENT_SEARCHES)
        }
    }

    private suspend fun addSearch(key: Preferences.Key<String>, query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return

        context.recentSearchDataStore.edit { preferences ->
            val currentList = parseSearches(preferences[key] ?: "").toMutableList()
            currentList.removeAll { it.equals(trimmed, ignoreCase = true) }
            currentList.add(0, trimmed)
            val updated = currentList.take(MAX_RECENT_SEARCHES)
            preferences[key] = updated.joinToString(DELIMITER)
        }
    }

    private suspend fun removeSearch(key: Preferences.Key<String>, query: String) {
        context.recentSearchDataStore.edit { preferences ->
            val currentList = parseSearches(preferences[key] ?: "").toMutableList()
            currentList.removeAll { it.equals(query.trim(), ignoreCase = true) }
            preferences[key] = currentList.joinToString(DELIMITER)
        }
    }

    fun parseSearches(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        return raw.split(DELIMITER)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .take(MAX_RECENT_SEARCHES)
    }
}