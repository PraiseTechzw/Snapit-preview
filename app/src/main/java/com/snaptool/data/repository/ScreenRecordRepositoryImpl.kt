package com.snaptool.data.repository

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.WindowManager
import com.snaptool.domain.model.RecorderState
import com.snaptool.domain.repository.ScreenRecordRepository
import com.snaptool.service.ScreenRecordService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenRecordRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ScreenRecordRepository {

    private val _recorderState = MutableStateFlow(RecorderState.IDLE)
    override val recorderState: StateFlow<RecorderState> = _recorderState

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null

    private val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

    override fun startRecording(resultCode: Int, data: Intent, audioEnabled: Boolean) {
        if (_recorderState.value != RecorderState.IDLE) return

        _recorderState.value = RecorderState.PREPARING

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        val screenDensity = metrics.densityDpi

        mediaProjection = projectionManager.getMediaProjection(resultCode, data)
        
        setupMediaRecorder(screenWidth, screenHeight, audioEnabled)

        try {
            mediaRecorder?.prepare()
            virtualDisplay = mediaProjection?.createVirtualDisplay(
                "SnapToolScreen",
                screenWidth,
                screenHeight,
                screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder?.surface,
                null,
                null
            )
            mediaRecorder?.start()
            _recorderState.value = RecorderState.RECORDING_SCREEN

            // Start Foreground Service
            val serviceIntent = Intent(context, ScreenRecordService::class.java).apply {
                putExtra(ScreenRecordService.EXTRA_RESULT_CODE, resultCode)
                putExtra(ScreenRecordService.EXTRA_DATA, data)
                putExtra(ScreenRecordService.EXTRA_AUDIO, audioEnabled)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            _recorderState.value = RecorderState.ERROR
            cleanup()
        }
    }

    override fun stopRecording() {
        if (_recorderState.value != RecorderState.RECORDING_SCREEN) return
        
        _recorderState.value = RecorderState.STOPPING
        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            // ignore
        }
        cleanup()
        _recorderState.value = RecorderState.IDLE
        
        val serviceIntent = Intent(context, ScreenRecordService::class.java).apply {
            action = ScreenRecordService.ACTION_STOP
        }
        context.startService(serviceIntent)
    }

    private fun setupMediaRecorder(width: Int, height: Int, audioEnabled: Boolean) {
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }

        val name = "SCR_" + SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/SnapTool")
            }
        }

        val uri = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
        val pfd = context.contentResolver.openFileDescriptor(uri!!, "rw")

        mediaRecorder?.apply {
            if (audioEnabled) {
                setAudioSource(MediaRecorder.AudioSource.MIC)
            }
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(pfd?.fileDescriptor)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            if (audioEnabled) {
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            }
            setVideoSize(width, height)
            setVideoFrameRate(30)
            setVideoEncodingBitRate(5 * 1024 * 1024)
        }
    }

    private fun cleanup() {
        virtualDisplay?.release()
        virtualDisplay = null
        mediaRecorder?.release()
        mediaRecorder = null
        mediaProjection?.stop()
        mediaProjection = null
    }
}
