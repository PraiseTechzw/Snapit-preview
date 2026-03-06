package com.snaptool.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.pm.ServiceInfoCompat
import com.snaptool.domain.model.RecorderState
import com.snaptool.domain.repository.ScreenRecordRepository
import com.snaptool.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

/**
 * ScreenRecordService — the single owner of MediaProjection, MediaRecorder, and VirtualDisplay.
 *
 * Critical ordering that avoids both crash types:
 *
 *  1. [ContextCompat.startForegroundService] called from MainActivity (gives us 5 s).
 *  2. [ServiceCompat.startForeground] with FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION called
 *     at the very top of [onStartCommand] — BEFORE any projection API is touched.
 *  3. Only after startForeground() returns do we call [MediaProjectionManager.getMediaProjection],
 *     satisfying the OS security requirement.
 *
 * Neither the ViewModel nor the Repository touches MediaProjection directly.
 * They only mutate [recorderState] (exposed via the repository's StateFlow) so the UI reacts.
 */
@AndroidEntryPoint
class ScreenRecordService : Service() {

    // ── Injected ──────────────────────────────────────────────────────────────
    @Inject lateinit var screenRecordRepository: ScreenRecordRepository

    // ── Recording resources (owned solely by this service) ─────────────────
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay?   = null
    private var mediaRecorder: MediaRecorder?     = null

    // ── MediaProjection callback — stops recording when projection is revoked ─
    private val projectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            Log.w(TAG, "MediaProjection stopped externally")
            stopRecordingAndSelf()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> handleStart(intent)
            ACTION_STOP  -> stopRecordingAndSelf()
            else         -> { Log.w(TAG, "Unknown action: ${intent?.action}"); stopSelf() }
        }
        return START_NOT_STICKY
    }

    // ── Start handler ─────────────────────────────────────────────────────────

    private fun handleStart(intent: Intent) {
        val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, -1)
        val data: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_DATA, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_DATA)
        }
        val audioEnabled = intent.getBooleanExtra(EXTRA_AUDIO, false)

        if (resultCode == -1 || data == null) {
            Log.e(TAG, "Missing resultCode or data Intent — cannot start recording")
            stopSelf()
            return
        }

        // ── STEP 1: Promote to foreground IMMEDIATELY ────────────────────────
        // This MUST happen before getMediaProjection(). ServiceCompat handles the
        // API level differences for the foreground service type flag.
        ServiceCompat.startForeground(
            /* service    = */ this,
            /* id         = */ NOTIFICATION_ID,
            /* notification = */ buildNotification(),
            /* foregroundServiceType = */
            ServiceInfoCompat.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
        )

        // ── STEP 2: Obtain MediaProjection — now safe after startForeground ──
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val projection = try {
            projectionManager.getMediaProjection(resultCode, data)
        } catch (e: SecurityException) {
            Log.e(TAG, "getMediaProjection failed: ${e.message}")
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        if (projection == null) {
            Log.e(TAG, "getMediaProjection returned null")
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        mediaProjection = projection
        projection.registerCallback(projectionCallback, null)

        // ── STEP 3: Set up MediaRecorder and VirtualDisplay ──────────────────
        startRecording(projection, audioEnabled)
    }

    // ── Recording logic ───────────────────────────────────────────────────────

    private fun startRecording(projection: MediaProjection, audioEnabled: Boolean) {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(metrics)
        val width   = metrics.widthPixels
        val height  = metrics.heightPixels
        val density = metrics.densityDpi

        val recorder = buildMediaRecorder(width, height, audioEnabled) ?: run {
            Log.e(TAG, "Failed to build MediaRecorder")
            cleanup()
            stopSelf()
            return
        }
        mediaRecorder = recorder

        try {
            recorder.prepare()
        } catch (e: Exception) {
            Log.e(TAG, "MediaRecorder.prepare() failed: ${e.message}")
            cleanup()
            stopSelf()
            return
        }

        virtualDisplay = projection.createVirtualDisplay(
            "SnapToolScreen",
            width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            recorder.surface,
            null, null
        )

        try {
            recorder.start()
        } catch (e: Exception) {
            Log.e(TAG, "MediaRecorder.start() failed: ${e.message}")
            cleanup()
            stopSelf()
            return
        }

        // Publish state change to UI via repository's StateFlow
        screenRecordRepository.setRecorderState(RecorderState.RECORDING_SCREEN)
        Log.i(TAG, "Screen recording started")
    }

    private fun buildMediaRecorder(width: Int, height: Int, audioEnabled: Boolean): MediaRecorder? {
        return try {
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            val name = "SCR_" + SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US)
                .format(System.currentTimeMillis())

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/SnapTool")
                }
            }

            val uri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
            val pfd = contentResolver.openFileDescriptor(uri!!, "rw")

            recorder.apply {
                if (audioEnabled) setAudioSource(MediaRecorder.AudioSource.MIC)
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(pfd?.fileDescriptor)
                setVideoSize(width, height)
                setVideoFrameRate(30)
                setVideoEncodingBitRate(5 * 1024 * 1024)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                if (audioEnabled) setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            }
            recorder
        } catch (e: Exception) {
            Log.e(TAG, "buildMediaRecorder exception: ${e.message}")
            null
        }
    }

    // ── Stop handler ──────────────────────────────────────────────────────────

    private fun stopRecordingAndSelf() {
        screenRecordRepository.setRecorderState(RecorderState.STOPPING)
        cleanup()
        screenRecordRepository.setRecorderState(RecorderState.IDLE)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun cleanup() {
        try { mediaRecorder?.stop() } catch (_: Exception) {}
        mediaRecorder?.release()
        mediaRecorder = null

        virtualDisplay?.release()
        virtualDisplay = null

        mediaProjection?.unregisterCallback(projectionCallback)
        mediaProjection?.stop()
        mediaProjection = null

        Log.i(TAG, "Resources cleaned up")
    }

    override fun onDestroy() {
        cleanup()
        super.onDestroy()
    }

    // ── Notification ──────────────────────────────────────────────────────────

    private fun buildNotification(): Notification {
        val stopIntent = buildStopIntent(this)
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val mainPendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Screen Recording")
            .setContentText("Recording in progress…")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentIntent(mainPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Screen Recording",
                NotificationManager.IMPORTANCE_LOW   // LOW = no sound, less intrusive
            ).apply {
                description = "Shown while a screen recording is in progress"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    // ── Companion ─────────────────────────────────────────────────────────────

    companion object {
        private const val TAG           = "ScreenRecordService"
        private const val CHANNEL_ID    = "screen_record_channel"
        private const val NOTIFICATION_ID = 101

        const val ACTION_START = "com.snaptool.ACTION_START_RECORDING"
        const val ACTION_STOP  = "com.snaptool.ACTION_STOP_RECORDING"

        private const val EXTRA_RESULT_CODE = "EXTRA_RESULT_CODE"
        private const val EXTRA_DATA        = "EXTRA_DATA"
        private const val EXTRA_AUDIO       = "EXTRA_AUDIO"

        /** Build the Intent that MainActivity passes to ContextCompat.startForegroundService(). */
        fun buildStartIntent(context: Context, resultCode: Int, data: Intent): Intent =
            Intent(context, ScreenRecordService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_RESULT_CODE, resultCode)
                putExtra(EXTRA_DATA, data)
                // audioEnabled will be read inside the service from settings if desired;
                // for simplicity we default to false here and let the service read it.
                putExtra(EXTRA_AUDIO, false)
            }

        /** Build the stop Intent (used by notification action and ViewModel). */
        fun buildStopIntent(context: Context): Intent =
            Intent(context, ScreenRecordService::class.java).apply {
                action = ACTION_STOP
            }
    }
}
