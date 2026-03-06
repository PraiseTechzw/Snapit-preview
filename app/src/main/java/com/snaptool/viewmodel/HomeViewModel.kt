package com.snaptool.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snaptool.domain.model.RecorderState
import com.snaptool.domain.repository.ScreenRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val screenRecordRepository: ScreenRecordRepository
) : ViewModel() {

    val recorderState: StateFlow<RecorderState> = screenRecordRepository.recorderState

}
