package com.snaptool.domain.repository

import androidx.camera.core.ImageCapture
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import kotlinx.coroutines.flow.Flow

interface CameraRepository {
    suspend fun takePhoto(prefix: String): Result<Unit>
    suspend fun startVideoRecording(audioEnabled: Boolean): Result<Unit>
    suspend fun stopVideoRecording()
    fun getVideoRecordEvents(): Flow<VideoRecordEvent>
}
