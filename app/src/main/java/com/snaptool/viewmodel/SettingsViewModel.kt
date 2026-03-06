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

    val photoPrefix =
            settingsRepository
                    .getPhotoPrefix()
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "IMG_")

    val audioEnabled =
            settingsRepository
                    .isAudioEnabled()
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val videoQuality =
            settingsRepository
                    .getVideoQuality()
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "HD")

    val overlayEnabled =
            settingsRepository
                    .isOverlayEnabled()
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setPhotoPrefix(prefix: String) {
        viewModelScope.launch { settingsRepository.setPhotoPrefix(prefix) }
    }

    fun setAudioEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAudioEnabled(enabled) }
    }

    fun setVideoQuality(quality: String) {
        viewModelScope.launch { settingsRepository.setVideoQuality(quality) }
    }

    fun setOverlayEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setOverlayEnabled(enabled) }
    }
}
