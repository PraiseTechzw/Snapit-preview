package com.snaptool.domain.model

import android.net.Uri

enum class MediaType {
    IMAGE, VIDEO
}

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val name: String,
    val type: MediaType,
    val dateAdded: Long,
    val size: Long,
    val mimeType: String,
    val duration: Long? = null
)

enum class RecorderState {
    IDLE,
    PREPARING,
    RECORDING_PHOTO,
    RECORDING_VIDEO,
    RECORDING_SCREEN,
    STOPPING,
    SUCCESS,
    ERROR
}

enum class CaptureMode {
    PHOTO, VIDEO
}
