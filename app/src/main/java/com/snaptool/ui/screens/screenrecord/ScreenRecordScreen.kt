package com.snaptool.ui.screens.screenrecord

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.snaptool.domain.model.RecorderState
import com.snaptool.ui.theme.*
import com.snaptool.viewmodel.ScreenRecordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenRecordScreen(
    onBack: () -> Unit,
    onLaunchProjection: (Intent, Boolean) -> Unit,
    viewModel: ScreenRecordViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val recorderState by viewModel.recorderState.collectAsState()
    val audioEnabled  by viewModel.audioEnabled.collectAsState(initial = true)
    val isRecording   = recorderState == RecorderState.RECORDING_SCREEN
    val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

    // Pulsing ring animation when recording
    val infiniteTransition = rememberInfiniteTransition(label = "recPulse")
    val ringScale by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.25f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringScale"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.6f,
        targetValue   = 0f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Surface0, Color(0xFF0D0D22)))
            )
    ) {
        // Background orbs
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-60).dp)
                .background(
                    Brush.radialGradient(listOf(Cyan50.copy(alpha = 0.12f), Color.Transparent)),
                    CircleShape
                )
                .blur(70.dp)
        )
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-60).dp, y = 60.dp)
                .background(
                    Brush.radialGradient(listOf(Indigo50.copy(alpha = 0.18f), Color.Transparent)),
                    CircleShape
                )
                .blur(60.dp)
        )

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // ── Top bar ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick  = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceGlass)
                        .border(1.dp, OutlineGlass, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Indigo80)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text  = "Screen Recording",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFE0E0F8),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.weight(0.8f))

            // ── Central icon with pulse ring ──────────────────────────────
            Box(
                modifier         = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Outer pulse ring (only when recording)
                if (isRecording) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .scale(ringScale)
                            .background(RecordRed.copy(alpha = ringAlpha * 0.3f), CircleShape)
                    )
                }

                // Icon container
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(
                            if (isRecording)
                                Brush.radialGradient(listOf(RecordRed.copy(0.8f), Color(0xFF7A0000)))
                            else
                                Brush.radialGradient(listOf(Cyan50.copy(0.8f), Indigo50))
                        )
                        .border(2.dp, if (isRecording) RecordRed.copy(0.6f) else Cyan50.copy(0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector  = if (isRecording) Icons.Default.StopCircle else Icons.Default.Screenshot,
                        contentDescription = null,
                        tint         = Color.White,
                        modifier     = Modifier.size(60.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── Status text ───────────────────────────────────────────────
            Column(
                modifier              = Modifier.fillMaxWidth(),
                horizontalAlignment   = Alignment.CenterHorizontally
            ) {
                Text(
                    text  = if (isRecording) "Recording in Progress" else "Ready to Record",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (isRecording) RecordRed else Color(0xFFE0E0F8),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text  = if (isRecording)
                        "Your screen is being captured"
                    else
                        "Tap below to start capturing your screen",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF8888BB)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // ── Info chips ────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                InfoChip(icon = Icons.Default.Mic,         label = "Audio")
                Spacer(modifier = Modifier.width(12.dp))
                InfoChip(icon = Icons.Default.Hd,          label = "HD Quality")
                Spacer(modifier = Modifier.width(12.dp))
                InfoChip(icon = Icons.Default.VideoFile,   label = "MP4")
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── CTA button ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 32.dp)
            ) {
                if (isRecording) {
                    Button(
                        onClick = { viewModel.stopRecording() },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape    = RoundedCornerShape(18.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = RecordRed
                        )
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Stop Recording", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
                } else {
                    Button(
                        onClick = { onLaunchProjection(projectionManager.createScreenCaptureIntent(), audioEnabled) },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape    = RoundedCornerShape(18.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(listOf(Cyan50, Indigo60)),
                                    RoundedCornerShape(18.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FiberManualRecord, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Start Recording", style = MaterialTheme.typography.labelLarge, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(SurfaceGlass)
            .border(1.dp, OutlineGlass, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Cyan70, modifier = Modifier.size(14.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFFAAAAAA))
    }
}
