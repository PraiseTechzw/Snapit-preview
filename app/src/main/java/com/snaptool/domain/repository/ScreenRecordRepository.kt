package com.snaptool.domain.repository

import android.content.Intent
import com.snaptool.domain.model.RecorderState
import kotlinx.coroutines.flow.StateFlow

interface ScreenRecordRepository {
    val recorderState: StateFlow<RecorderState>
    fun startRecording(resultCode: Int, data: Intent, audioEnabled: Boolean)
    fun stopRecording()
}
