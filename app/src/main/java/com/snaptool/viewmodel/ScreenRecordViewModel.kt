package com.snaptool.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snaptool.domain.model.RecorderState
import com.snaptool.domain.repository.ScreenRecordRepository
import com.snaptool.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScreenRecordViewModel @Inject constructor(
    private val screenRecordRepository: ScreenRecordRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val recorderState: StateFlow<RecorderState> = screenRecordRepository.recorderState

    fun startRecording(resultCode: Int, data: Intent) {
        viewModelScope.launch {
            val audioEnabled = settingsRepository.isAudioEnabled().first()
            screenRecordRepository.startRecording(resultCode, data, audioEnabled)
        }
    }

    fun stopRecording() {
        screenRecordRepository.stopRecording()
    }
}
