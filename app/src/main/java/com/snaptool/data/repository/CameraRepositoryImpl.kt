package com.snaptool.data.repository

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.snaptool.domain.repository.CameraRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class CameraRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CameraRepository {

    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private val _videoEvents = MutableSharedFlow<VideoRecordEvent>()

    fun setImageCapture(capture: ImageCapture) {
        imageCapture = capture
    }

    fun setVideoCapture(capture: VideoCapture<Recorder>) {
        videoCapture = capture
    }

    override suspend fun takePhoto(prefix: String): Result<Unit> = suspendCancellableCoroutine { continuation ->
        val name = prefix + SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Snapit")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    continuation.resume(Result.success(Unit))
                }

                override fun onError(exception: ImageCaptureException) {
                    continuation.resume(Result.failure(exception))
                }
            }
        ) ?: continuation.resume(Result.failure(IllegalStateException("Camera not initialized")))
    }

    override suspend fun startVideoRecording(audioEnabled: Boolean): Result<Unit> {
        val name = "VID_" + SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Snapit")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()

        val videoCaptureLocal = videoCapture ?: return Result.failure(IllegalStateException("Camera not initialized"))

        return try {
            val pendingRecording = videoCaptureLocal.output.prepareRecording(context, mediaStoreOutputOptions)
            if (audioEnabled) {
                // Audio permission check should be handled before calling this
                // PendingRecording.withAudioEnabled() requires RECORD_AUDIO permission
                try {
                    pendingRecording.withAudioEnabled()
                } catch (e: SecurityException) {
                    Log.e("CameraRepo", "Audio recording failed due to missing permission", e)
                }
            }

            recording = pendingRecording.start(ContextCompat.getMainExecutor(context)) { event ->
                _videoEvents.tryEmit(event)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun stopVideoRecording() {
        recording?.stop()
        recording = null
    }

    override fun getVideoRecordEvents(): Flow<VideoRecordEvent> = _videoEvents
}
