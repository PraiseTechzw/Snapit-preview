package com.snaptool.ui.screens.camera

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.snaptool.domain.model.CaptureMode
import com.snaptool.domain.model.RecorderState
import com.snaptool.ui.components.CameraPreview
import com.snaptool.viewmodel.CameraViewModel

@Composable
fun CameraScreen(onBack: () -> Unit, viewModel: CameraViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val recorderState by viewModel.recorderState.collectAsState()
    val captureMode by viewModel.captureMode.collectAsState()
    val duration by viewModel.recordingDuration.collectAsState()

    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    LaunchedEffect(cameraSelector) {
        val cameraProvider = cameraProviderFuture.get()
        val imageCapture = ImageCapture.Builder().build()
        val recorder = Recorder.Builder().build()
        val videoCapture = VideoCapture.withOutput(recorder)

        viewModel.initializeCamera(imageCapture, videoCapture)

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    imageCapture,
                    videoCapture
            )
        } catch (e: Exception) {
            // handle error
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
                modifier = Modifier.fillMaxSize(),
                onPreviewReady = { preview, _ ->
                    val cameraProvider = cameraProviderFuture.get()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                }
        )

        // UI Overlays
        Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                if (recorderState == RecorderState.RECORDING_VIDEO) {
                    Text(
                            text = formatDuration(duration),
                            color = Color.Red,
                            style = MaterialTheme.typography.titleLarge,
                            modifier =
                                    Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mode Selector
                Row(
                        modifier =
                                Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                        .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                            selected = captureMode == CaptureMode.PHOTO,
                            onClick = { viewModel.setCaptureMode(CaptureMode.PHOTO) },
                            label = { Text("Photo", color = Color.White) }
                    )
                    FilterChip(
                            selected = captureMode == CaptureMode.VIDEO,
                            onClick = { viewModel.setCaptureMode(CaptureMode.VIDEO) },
                            label = { Text("Video", color = Color.White) }
                    )
                }

                // Controls
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                            onClick = {
                                cameraSelector =
                                        if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                            CameraSelector.DEFAULT_FRONT_CAMERA
                                        } else {
                                            CameraSelector.DEFAULT_BACK_CAMERA
                                        }
                            },
                            enabled = recorderState != RecorderState.RECORDING_VIDEO
                    ) {
                        Icon(
                                Icons.Default.FlipCameraAndroid,
                                contentDescription = "Flip",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                        )
                    }

                    CaptureButton(
                            recorderState = recorderState,
                            captureMode = captureMode,
                            onClick = {
                                if (captureMode == CaptureMode.PHOTO) {
                                    viewModel.takePhoto()
                                } else {
                                    viewModel.toggleVideoRecording()
                                }
                            }
                    )

                    IconButton(onClick = { /* Flush toggle logic */}) {
                        Icon(
                                Icons.Default.FlashOn,
                                contentDescription = "Flash",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CaptureButton(recorderState: RecorderState, captureMode: CaptureMode, onClick: () -> Unit) {
    val color =
            if (captureMode == CaptureMode.VIDEO) {
                if (recorderState == RecorderState.RECORDING_VIDEO) Color.Red else Color.White
            } else Color.White

    Button(
            onClick = onClick,
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = color),
            contentPadding = PaddingValues(0.dp)
    ) {
        if (recorderState == RecorderState.RECORDING_VIDEO) {
            Box(Modifier.size(30.dp).background(Color.White, MaterialTheme.shapes.small))
        } else if (recorderState == RecorderState.RECORDING_PHOTO) {
            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(40.dp))
        }
    }
}

fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}
