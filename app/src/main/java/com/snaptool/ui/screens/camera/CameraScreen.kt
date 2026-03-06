package com.snaptool.ui.screens.camera

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.snaptool.domain.model.CaptureMode
import com.snaptool.domain.model.RecorderState
import com.snaptool.ui.components.CameraPreview
import com.snaptool.ui.theme.*
import com.snaptool.viewmodel.CameraViewModel

@Composable
fun CameraScreen(onBack: () -> Unit, viewModel: CameraViewModel = hiltViewModel()) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val recorderState  by viewModel.recorderState.collectAsState()
    val captureMode    by viewModel.captureMode.collectAsState()
    val duration       by viewModel.recordingDuration.collectAsState()

    val isRecordingVideo = recorderState == RecorderState.RECORDING_VIDEO

    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    LaunchedEffect(cameraSelector) {
        val cameraProvider = cameraProviderFuture.get()
        val imageCapture   = ImageCapture.Builder().build()
        val recorder       = Recorder.Builder().build()
        val videoCapture   = VideoCapture.withOutput(recorder)
        viewModel.initializeCamera(imageCapture, videoCapture)
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture, videoCapture)
        } catch (_: Exception) {}
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Camera preview fills entire screen
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onPreviewReady = { preview, _ ->
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
            }
        )

        // ── Gradient scrim at top ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(
                    Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent))
                )
        )
        // ── Gradient scrim at bottom ───────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)))
                )
        )

        // ── Top controls ───────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            CamIconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(22.dp))
            }

            // Recording timer pill
            if (isRecordingVideo) {
                val infiniteTransition = rememberInfiniteTransition(label = "recPulse")
                val dotAlpha by infiniteTransition.animateFloat(
                    initialValue  = 1f,
                    targetValue   = 0.2f,
                    animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
                    label         = "dotAlpha"
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .border(1.dp, RecordRed.copy(alpha = 0.4f), RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).background(RecordRed.copy(alpha = dotAlpha), CircleShape))
                    Text(
                        text  = formatDuration(duration),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            CamIconButton(onClick = { /* flash toggle */ }) {
                Icon(Icons.Default.FlashOn, "Flash", tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }

        // ── Bottom controls ────────────────────────────────────────────────
        Column(
            modifier              = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.spacedBy(20.dp)
        ) {
            // Mode selector pills
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(50))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ModeChip(
                    label    = "Photo",
                    selected = captureMode == CaptureMode.PHOTO,
                    onClick  = { viewModel.setCaptureMode(CaptureMode.PHOTO) }
                )
                ModeChip(
                    label    = "Video",
                    selected = captureMode == CaptureMode.VIDEO,
                    onClick  = { viewModel.setCaptureMode(CaptureMode.VIDEO) }
                )
            }

            // Shutter row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Flip camera
                CamIconButton(
                    onClick  = {
                        if (!isRecordingVideo) {
                            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                                CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                        }
                    }
                ) {
                    Icon(Icons.Default.FlipCameraAndroid, "Flip", tint = Color.White, modifier = Modifier.size(28.dp))
                }

                // Main shutter button
                ShutterButton(
                    recorderState = recorderState,
                    captureMode   = captureMode,
                    onClick       = {
                        if (captureMode == CaptureMode.PHOTO) viewModel.takePhoto()
                        else viewModel.toggleVideoRecording()
                    }
                )

                // Gallery shortcut
                CamIconButton(onClick = { /* navigate to gallery */ }) {
                    Icon(Icons.Default.PhotoLibrary, "Gallery", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

// ── Shutter Button ─────────────────────────────────────────────────────────────
@Composable
fun ShutterButton(recorderState: RecorderState, captureMode: CaptureMode, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label         = "shutterScale"
    )

    val isRecording = recorderState == RecorderState.RECORDING_VIDEO
    val ringColor   = if (captureMode == CaptureMode.VIDEO) RecordRed else Color.White

    Box(
        modifier         = Modifier.size(82.dp).scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Outer ring
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .border(3.dp, ringColor.copy(alpha = if (isRecording) 1f else 0.7f), CircleShape)
        )

        // Inner button
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(if (isRecording) RoundedCornerShape(10.dp) else CircleShape)
                .background(
                    if (isRecording) RecordRed
                    else if (captureMode == CaptureMode.VIDEO) RecordRed
                    else Color.White
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication        = rememberRipple(color = Color.Black.copy(0.2f)),
                    onClick           = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            if (recorderState == RecorderState.RECORDING_PHOTO) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(30.dp), strokeWidth = 3.dp)
            }
        }
    }
}

// ── Mode Chip ─────────────────────────────────────────────────────────────────
@Composable
private fun ModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.Black else Color.White.copy(alpha = 0.7f),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ── Camera Icon Button ────────────────────────────────────────────────────────
@Composable
private fun CamIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}
