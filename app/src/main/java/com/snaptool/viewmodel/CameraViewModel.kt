package com.snaptool.viewmodel

import androidx.camera.core.ImageCapture
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snaptool.data.repository.CameraRepositoryImpl
import com.snaptool.domain.model.CaptureMode
import com.snaptool.domain.model.RecorderState
import com.snaptool.domain.repository.CameraRepository
import com.snaptool.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val cameraRepository: CameraRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _recorderState = MutableStateFlow(RecorderState.IDLE)
    val recorderState = _recorderState.asStateFlow()

    private val _captureMode = MutableStateFlow(CaptureMode.PHOTO)
    val captureMode = _captureMode.asStateFlow()

    private val _recordingDuration = MutableStateFlow(0L)
    val recordingDuration = _recordingDuration.asStateFlow()

    init {
        viewModelScope.launch {
            cameraRepository.getVideoRecordEvents().collect { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        _recorderState.value = RecorderState.RECORDING_VIDEO
                    }
                    is VideoRecordEvent.Finalize -> {
                        _recorderState.value = RecorderState.IDLE
                        _recordingDuration.value = 0L
                    }
                    is VideoRecordEvent.Status -> {
                        _recordingDuration.value = event.recordingStats.recordedDurationNanos / 1_000_000_000
                    }
                }
            }
        }
    }

    fun setCaptureMode(mode: CaptureMode) {
        _captureMode.value = mode
    }

    fun initializeCamera(imageCapture: ImageCapture, videoCapture: VideoCapture<Recorder>) {
        (cameraRepository as CameraRepositoryImpl).setImageCapture(imageCapture)
        (cameraRepository as CameraRepositoryImpl).setVideoCapture(videoCapture)
    }

    fun takePhoto() {
        viewModelScope.launch {
            _recorderState.value = RecorderState.RECORDING_PHOTO
            val prefix = settingsRepository.getPhotoPrefix().first()
            val result = cameraRepository.takePhoto(prefix)
            _recorderState.value = if (result.isSuccess) RecorderState.SUCCESS else RecorderState.ERROR
            // Reset to idle after a short delay
            kotlinx.coroutines.delay(1000)
            _recorderState.value = RecorderState.IDLE
        }
    }

    fun toggleVideoRecording() {
        viewModelScope.launch {
            if (_recorderState.value == RecorderState.RECORDING_VIDEO) {
                cameraRepository.stopVideoRecording()
            } else {
                val audioEnabled = settingsRepository.isAudioEnabled().first()
                cameraRepository.startVideoRecording(audioEnabled)
            }
        }
    }
}
