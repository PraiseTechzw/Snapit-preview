package com.snaptool.domain.repository

import com.snaptool.domain.model.MediaItem
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun getMediaItems(): Flow<List<MediaItem>>
    suspend fun deleteMediaItem(item: MediaItem): Boolean
}

interface SettingsRepository {
    fun getPhotoPrefix(): Flow<String>
    suspend fun setPhotoPrefix(prefix: String)
    fun isAudioEnabled(): Flow<Boolean>
    suspend fun setAudioEnabled(enabled: Boolean)
    fun getVideoQuality(): Flow<String>
    suspend fun setVideoQuality(quality: String)
}
