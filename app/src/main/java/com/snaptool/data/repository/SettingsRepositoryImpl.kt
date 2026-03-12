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
        val SCREENSHOT_PREFIX = stringPreferencesKey("screenshot_prefix")
        val AUDIO_ENABLED = booleanPreferencesKey("audio_enabled")
        val RECORD_QUALITY = stringPreferencesKey("record_quality")
        val OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
    }

    override fun getScreenshotPrefix(): Flow<String> = context.dataStore.data.map { 
        it[PreferencesKeys.SCREENSHOT_PREFIX] ?: "SHOT_"
    }

    override suspend fun setScreenshotPrefix(prefix: String) {
        context.dataStore.edit { it[PreferencesKeys.SCREENSHOT_PREFIX] = prefix }
    }

    override fun isAudioEnabled(): Flow<Boolean> = context.dataStore.data.map {
        it[PreferencesKeys.AUDIO_ENABLED] ?: true
    }

    override suspend fun setAudioEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.AUDIO_ENABLED] = enabled }
    }

    override fun getRecordQuality(): Flow<String> = context.dataStore.data.map {
        it[PreferencesKeys.RECORD_QUALITY] ?: "HD"
    }

    override suspend fun setRecordQuality(quality: String) {
        context.dataStore.edit { it[PreferencesKeys.RECORD_QUALITY] = quality }
    }

    override fun isOverlayEnabled(): Flow<Boolean> = context.dataStore.data.map {
        it[PreferencesKeys.OVERLAY_ENABLED] ?: false
    }

    override suspend fun setOverlayEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.OVERLAY_ENABLED] = enabled }
    }
}
