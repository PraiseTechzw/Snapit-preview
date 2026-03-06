package com.snaptool.viewmodel

import androidx.lifecycle.ViewModel
import com.snaptool.domain.model.RecorderState
import com.snaptool.domain.repository.ScreenRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * ScreenRecordViewModel — thin UI state bridge.
 *
 * This ViewModel has NO knowledge of resultCode, data Intent, or MediaProjection.
 * Its only jobs are:
 *  1. Expose [recorderState] so the UI can react to recording state changes.
 *  2. Delegate stop requests to the repository (which forwards to the service).
 *
 * Starting a recording is triggered from [ScreenRecordScreen] by calling
 * [MainActivity.onLaunchProjection]. The Activity result handler starts the
 * foreground service directly, bypassing this ViewModel entirely for the sensitive
 * projection handshake — this is the correct Android architecture pattern.
 */
@HiltViewModel
class ScreenRecordViewModel @Inject constructor(
    private val screenRecordRepository: ScreenRecordRepository
) : ViewModel() {

    /** Observed by the UI to show recording indicator, stop button, etc. */
    val recorderState: StateFlow<RecorderState> = screenRecordRepository.recorderState

    /**
     * Called by the UI when the user taps "Stop Recording".
     * Sends ACTION_STOP to the running service via the repository.
     */
    fun stopRecording() {
        screenRecordRepository.requestStop()
    }
}
