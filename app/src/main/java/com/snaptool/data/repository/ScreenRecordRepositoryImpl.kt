package com.snaptool.data.repository

import android.content.Context
import android.content.Intent
import com.snaptool.domain.model.RecorderState
import com.snaptool.domain.repository.ScreenRecordRepository
import com.snaptool.service.ScreenRecordService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ScreenRecordRepositoryImpl — pure state store.
 *
 * This class intentionally does NOT obtain MediaProjection, create a VirtualDisplay,
 * or operate a MediaRecorder. Those responsibilities have been moved to
 * [ScreenRecordService], which is the correct owner because:
 *
 *  - Only a foreground Service with foregroundServiceType="mediaProjection" may call
 *    MediaProjectionManager.getMediaProjection() on Android 10+.
 *  - A Repository is a singleton that survives beyond Android's foreground service
 *    lifecycle guarantees, making resource ownership ambiguous and crash-prone.
 *
 * The service calls [setRecorderState] to push state back to the UI without
 * needing the UI to poll.
 */
@Singleton
class ScreenRecordRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ScreenRecordRepository {

    private val _recorderState = MutableStateFlow(RecorderState.IDLE)
    override val recorderState: StateFlow<RecorderState> = _recorderState.asStateFlow()

    /**
     * Called by [ScreenRecordService] whenever the recording state changes.
     * Thread-safe — MutableStateFlow handles concurrent updates.
     */
    override fun setRecorderState(state: RecorderState) {
        _recorderState.value = state
    }

    /**
     * Sends ACTION_STOP to the running service so it can tear down resources
     * cleanly and call stopForeground()/stopSelf() itself.
     */
    override fun requestStop() {
        context.startService(ScreenRecordService.buildStopIntent(context))
    }
}
