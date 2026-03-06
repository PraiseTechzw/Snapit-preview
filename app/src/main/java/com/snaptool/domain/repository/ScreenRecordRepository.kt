package com.snaptool.domain.repository

import com.snaptool.domain.model.RecorderState
import kotlinx.coroutines.flow.StateFlow

/**
 * ScreenRecordRepository exposes recorder state to the UI layer.
 *
 * It intentionally has NO knowledge of MediaProjection, resultCode, or Intent data.
 * Those details belong exclusively in ScreenRecordService.
 *
 * [setRecorderState] is called by ScreenRecordService to push state updates so the
 * ViewModel/UI can react (show recording indicator, timer, etc.).
 */
interface ScreenRecordRepository {
    val recorderState: StateFlow<RecorderState>

    /**
     * Called by ScreenRecordService when the recording state changes.
     * Not intended to be called by the ViewModel.
     */
    fun setRecorderState(state: RecorderState)

    /**
     * Tells the service to stop via an Intent. The actual teardown is performed
     * inside ScreenRecordService to preserve correct ownership.
     */
    fun requestStop()
}
