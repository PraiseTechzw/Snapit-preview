package com.snaptool.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.snaptool.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private object PreferencesKeys {
        val PHOTO_PREFIX = stringPreferencesKey("photo_prefix")
        val AUDIO_ENABLED = booleanPreferencesKey("audio_enabled")
        val VIDEO_QUALITY = stringPreferencesKey("video_quality")
    }

    override fun getPhotoPrefix(): Flow<String> = context.dataStore.data.map { 
        it[PreferencesKeys.PHOTO_PREFIX] ?: "IMG_"
    }

    override suspend fun setPhotoPrefix(prefix: String) {
        context.dataStore.edit { it[PreferencesKeys.PHOTO_PREFIX] = prefix }
    }

    override fun isAudioEnabled(): Flow<Boolean> = context.dataStore.data.map {
        it[PreferencesKeys.AUDIO_ENABLED] ?: true
    }

    override suspend fun setAudioEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.AUDIO_ENABLED] = enabled }
    }

    override fun getVideoQuality(): Flow<String> = context.dataStore.data.map {
        it[PreferencesKeys.VIDEO_QUALITY] ?: "HD"
    }

    override suspend fun setVideoQuality(quality: String) {
        context.dataStore.edit { it[PreferencesKeys.VIDEO_QUALITY] = quality }
    }
}
