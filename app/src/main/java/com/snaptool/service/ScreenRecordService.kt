package com.snaptool.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.snaptool.R
import com.snaptool.data.repository.ScreenRecordRepositoryImpl
import com.snaptool.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ScreenRecordService : Service() {

    companion object {
        const val CHANNEL_ID = "screen_record_channel"
        const val NOTIFICATION_ID = 101
        const val ACTION_STOP = "ACTION_STOP_RECORDING"
        const val EXTRA_RESULT_CODE = "EXTRA_RESULT_CODE"
        const val EXTRA_DATA = "EXTRA_DATA"
        const val EXTRA_AUDIO = "EXTRA_AUDIO"
    }

    @Inject lateinit var screenRecordRepository: ScreenRecordRepositoryImpl

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            screenRecordRepository.stopRecordingInternal()
            stopSelf()
            return START_NOT_STICKY
        }

        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, -1) ?: -1
        val data =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent?.getParcelableExtra(EXTRA_DATA, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION") intent?.getParcelableExtra(EXTRA_DATA)
                }
        val audioEnabled = intent?.getBooleanExtra(EXTRA_AUDIO, false) ?: false

        if (resultCode == -1 || data == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Step 1: Start foreground FIRST with the correct service type.
        // This MUST happen before getMediaProjection() is called.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                    NOTIFICATION_ID,
                    createNotification(),
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }

        // Step 2: Now that the foreground service is running, begin actual recording.
        val projectionManager =
                getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val mediaProjection = projectionManager.getMediaProjection(resultCode, data)
        screenRecordRepository.startRecordingInternal(mediaProjection, audioEnabled)

        return START_STICKY
    }

    private fun createNotification(): Notification {
        val stopIntent =
                Intent(this, ScreenRecordService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent =
                PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val mainIntent = Intent(this, MainActivity::class.java)
        val mainPendingIntent =
                PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Screen Recording")
                .setContentText("Recording in progress...")
                .setSmallIcon(android.R.drawable.ic_menu_save)
                .setContentIntent(mainPendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
                .setOngoing(true)
                .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                    NotificationChannel(
                            CHANNEL_ID,
                            "Screen Recording",
                            NotificationManager.IMPORTANCE_DEFAULT
                    )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
