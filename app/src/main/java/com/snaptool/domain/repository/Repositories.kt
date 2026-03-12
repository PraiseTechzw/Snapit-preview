package com.snaptool.domain.repository

import com.snaptool.domain.model.MediaItem
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun getMediaItems(): Flow<List<MediaItem>>
    suspend fun deleteMediaItem(item: MediaItem): Boolean
}

interface SettingsRepository {
    fun getScreenshotPrefix(): Flow<String>
    suspend fun setScreenshotPrefix(prefix: String)
    fun isAudioEnabled(): Flow<Boolean>
    suspend fun setAudioEnabled(enabled: Boolean)
    fun getRecordQuality(): Flow<String>
    suspend fun setRecordQuality(quality: String)
    fun isOverlayEnabled(): Flow<Boolean>
    suspend fun setOverlayEnabled(enabled: Boolean)
}
