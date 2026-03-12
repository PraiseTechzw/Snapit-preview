package com.snaptool.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.snaptool.domain.repository.SettingsRepository
import com.snaptool.service.ScreenRecordService
import com.snaptool.ui.navigation.SnapitNavHost
import com.snaptool.ui.theme.SnapitTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * MainActivity owns the MediaProjection permission launcher.
 *
 * Architectural note:
 * - The ActivityResultLauncher for MEDIA_PROJECTION must live in an Activity because the system's
 * permission dialog is tied to an Activity result. We expose a companion-object lambda so
 * ScreenRecordScreen can trigger the launch without holding a direct Activity reference.
 * - After the user approves, we immediately start ScreenRecordService via
 * ContextCompat.startForegroundService() and forward the resultCode + data Intent to the service
 * via extras. The service then calls startForeground() first thing before touching MediaProjection.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository

    companion object {
        /**
         * Set by ScreenRecordScreen before launching the projection request. Called back on the
         * main thread after the system dialog resolves.
         */
        var onProjectionResult:
                ((resultCode: Int, data: android.content.Intent, audioEnabled: Boolean) -> Unit)? =
                null
    }

    private var currentAudioEnabled: Boolean = true
    private var currentIsScreenshot: Boolean = false

    private val projectionLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val data = result.data
                if (result.resultCode == RESULT_OK && data != null) {
                    // Forward directly to the service
                    if (currentIsScreenshot) {
                        startScreenshotService(result.resultCode, data)
                    } else {
                        startScreenRecordService(result.resultCode, data, currentAudioEnabled)
                    }

                    // Also notify the ViewModel via the Screen if needed
                    onProjectionResult?.invoke(result.resultCode, data, currentAudioEnabled)
                }
                onProjectionResult = null
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Auto-start OverlayService if enabled
        val overlayEnabled = runBlocking { settingsRepository.isOverlayEnabled().first() }
        if (overlayEnabled && android.provider.Settings.canDrawOverlays(this)) {
            ContextCompat.startForegroundService(
                    this,
                    android.content.Intent(this, com.snaptool.service.OverlayService::class.java)
            )
        }

        setContent {
            SnapitTheme {
                Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                ) {
                    SnapitNavHost(
                            onLaunchProjection = { intent, audioEnabled, isScreenshot ->
                                currentAudioEnabled = audioEnabled
                                currentIsScreenshot = isScreenshot
                                projectionLauncher.launch(intent)
                            },
                            initialUri = intent?.data
                    )
                }
            }
        }
    }

    /**
     * Starts ScreenRecordService as a foreground service. ContextCompat.startForegroundService() is
     * the correct API on O+ and handles the 5-second window within which the service must call
     * startForeground().
     */
    private fun startScreenRecordService(
            resultCode: Int,
            data: android.content.Intent,
            audioEnabled: Boolean
    ) {
        val serviceIntent =
                ScreenRecordService.buildStartIntent(
                        context = this,
                        resultCode = resultCode,
                        data = data,
                        audioEnabled = audioEnabled
                )
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun startScreenshotService(
            resultCode: Int,
            data: android.content.Intent
    ) {
        val serviceIntent =
                ScreenRecordService.buildScreenshotIntent(
                        context = this,
                        resultCode = resultCode,
                        data = data
                )
        ContextCompat.startForegroundService(this, serviceIntent)
    }
}
