package com.snaptool.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snaptool.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(private val settingsRepository: SettingsRepository) :
        ViewModel() {

    val screenshotPrefix =
            settingsRepository
                    .getScreenshotPrefix()
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SHOT_")

    val audioEnabled =
            settingsRepository
                    .isAudioEnabled()
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val recordQuality =
            settingsRepository
                    .getRecordQuality()
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "HD")

    val overlayEnabled =
            settingsRepository
                    .isOverlayEnabled()
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setScreenshotPrefix(prefix: String) {
        viewModelScope.launch { settingsRepository.setScreenshotPrefix(prefix) }
    }

    fun setAudioEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAudioEnabled(enabled) }
    }

    fun setRecordQuality(quality: String) {
        viewModelScope.launch { settingsRepository.setRecordQuality(quality) }
    }

    fun setOverlayEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setOverlayEnabled(enabled) }
    }
}
